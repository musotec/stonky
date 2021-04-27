package tech.muso.demo.graph.spark

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tech.muso.rekoil.core.Atom
import tech.muso.rekoil.core.RekoilScope
import java.lang.Exception
import java.util.*
import kotlin.math.max
import kotlin.math.min

@ExperimentalCoroutinesApi
open class RekoilQueue<T : Any>(
    rekoilScope: RekoilScope,
    private val initialValue: T,
    var capacity: Int = Int.MAX_VALUE
) :  RekoilScope by rekoilScope, LinkedList<T>() {
// FIXME: should not extend LinkedList directly; exposes methods, but only handles add & poll correctly!

    private val mutex = Mutex()
    private val owner = Any()

    val removed: Atom<T> = atom { initialValue }
    val added: Atom<T> = atom { initialValue }

    val end: Atom<T> = atom { initialValue }
    val start: Atom<T> = atom { initialValue }

    private var readChannel = Channel<Deferred<*>>(Channel.RENDEZVOUS)    // reads buffered, but may change // TODO: KEEP RENDEZVOUS IF withLock crash is fixed
    private var writeChannel = Channel<Deferred<*>>(Channel.BUFFERED)   // must buffer; every write needed!

    private suspend fun receive() = select<Unit> {
        // TODO: receive must block while we do not have the mutex
        // process the write channel before the read channel
        writeChannel.onReceive { value ->  // highest priority
//            println("write -> '$value'")
            mutex.withLock(owner) {
                value.await()
            }
        }

        readChannel.onReceive { value ->  // reads come secondary
//            println("read -> '$value'")
            mutex.withLock(owner) {
                value.await()
            }
        }
    }

    private fun restartReadChannel() {
        readChannel.close()
        readChannel = Channel(Channel.BUFFERED)
    }

    fun setList(list: List<T>) {
        // TODO: invalidateAll() on rekoil scope? -> shut down/restart any pending selectors

        useLock {
            // FIXME: this needs "ultra" priority, since it can have reads that are invalidated with a new dataset entirely!

            // update the capacity
            capacity = list.size
            // clear the backing list first
            clear()

            // iterating over each as we add (to linked list) is the same, but this updates heaps
            list.forEach {
                add(it)
            }
        }
    }

    override fun clear() {
        println("special clear")
        restartReadChannel()
        useLock {
            super.clear()
        }
    }

    override fun add(e: T): Boolean {
        var b = false

        useLock {
            // if at capacity, then remove the oldest node
            if (size >= capacity) {
                poll()
                start.value = first // and update the node at the start of the list
            } else if (size == 0) {
                start.value = e
            }

            b = super.add(e)
            added.value = e
            end.value = e
        }
        return b
    }

    // NOTE: final override because we cannot rely on the return value outside this scope!!!
    final override fun poll(): T? {
        var e: T? = null
        useLock {
            e = super.poll()
            if (e != null) removed.value = e as T
//            println("POLL: $e")
        }
        return e
    }

    fun useLock(function: () -> Unit) {
        // invoke the function if we own the lock right now; avoids extraneous deferred execution
        if (mutex.holdsLock(owner)) {
            println("HELD LOCK")
            function()
            return
        }

        val deferred = GlobalScope.async(start = CoroutineStart.LAZY) {
            rekoilContext.coroutineScope.launch {
                function()
            }
        }

        // FIXME: there will be a problem if two methods try to useLock() at the same time!!
        // writing operations should block until processed
        runBlocking {
            // write has priority, but sends to wait on any low priority executions to finish
            writeChannel.send(deferred)
            receive()   // and notify receive to process
        }
    }

    suspend fun <R> use(function: suspend () -> R): R? {
//        if (!mutex.holdsLock(owner)) return null

        // start deferrable lazily because we will wait for the processing channel to execute
        val deferred = GlobalScope.async(start = CoroutineStart.LAZY) {
            // make sure that we invoke with the outer most context for execution sync
            withContext(rekoilContext.coroutineScope.coroutineContext) { function() }
        }

        // place in the read channel
        readChannel.send(deferred)
        // and receive by suspending

        if (!mutex.holdsLock(owner)) {
            receive()        // FIXME: handle crash when mutex lock already in use
        }

        deferred.join()  // TODO: do we need to join()?
        return deferred.getCompleted()  // return the completed value
    }

    // FIXME: should not need to coerce size. Remove, fix, and ensure the previous reads are removed!
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
//        val first = min(fromIndex, toIndex)
//        val second = max(fromIndex, toIndex).coerceAtMost(max(0, size-1))
//        val start = min(first,second)
//        val end = max(first,second)
        try {
            return super.subList(fromIndex, toIndex)
        } catch (ex: Exception) {
            return super.subList(0,0)
        }
    }
}