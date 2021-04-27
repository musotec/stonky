package tech.muso.demo.graph.spark.types

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import tech.muso.demo.graph.core.CandleGraphable
import tech.muso.demo.graph.core.Graphable
import tech.muso.demo.graph.core.NullGraphable
import tech.muso.demo.graph.spark.RekoilQueue
import tech.muso.rekoil.core.Atom
import tech.muso.rekoil.core.RekoilScope
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class MinMaxGraphableQueue<T : Graphable>(
    rekoilScope: RekoilScope,
    private val initialValue: T,
    private val windows: Array<Int> = arrayOf(3,5),
    capacity: Int = Int.MAX_VALUE
) : RekoilQueue<T>(rekoilScope, initialValue, capacity) {

    @RequiresApi(Build.VERSION_CODES.N)
    val maxHeap = PriorityQueue<T> { a, b ->
        if (a == null) 1
        else if (b == null) -1
        else {
            println("[MAX] a: $a, b: $b")
            b.top.compareTo(a.top)
        }    // flip sign for max heap
    }

    @RequiresApi(Build.VERSION_CODES.N)
    val minHeap = PriorityQueue<T> { a, b ->
        if (a == null) 1
        else if (b == null) -1
        else {
            println("[min] a: $a, b: $b")
            a.bottom.compareTo(b.bottom)
        }
    }

    val min: Atom<Float> = atom { initialValue.bottom }
    val max: Atom<Float> = atom { initialValue.top }

//    val skipLists: Map<Int, FifoList<Graphable>> = buildMap {
//        windows.forEach { i ->
//            this[i] = FifoList<Graphable>(capacity)
//        }
//    }

    // FIXME: sub-optimal performance to skip list implementation (but need solution for max/min over skips)
    // FIXME: candles should shift out based on the window. so skip up to (n-1) candles at start.
    fun getWindow(window: Int): List<Graphable> {
        val result = windowed(window, step=window).map {
            if (it.first() is CandleGraphable) {
                val x = (it.first() as CandleGraphable).x
                val time = (it.first() as CandleGraphable).time
                val open = (it.first() as CandleGraphable).open
                val close = (it.last() as CandleGraphable).close
                val low = it.minOf { it.bottom }
                val high = it.maxOf { it.top }
                val volume = it.sumBy{ (it as CandleGraphable).volume }
                CandleGraphable(
                    x = x,
                    time = time,
                    open = open,
                    close = close,
                    high = high,
                    low = low,
                    volume = volume,
                    color= Color.MAGENTA
                )
            } else {
                it.first()
            }
        }

        return result
    }

    init {
//        selector {
//            val new = get(min)
//            val heap = minHeap.peek()
//            println("MIN: $new; HEAP: $heap")
//        }
//
//        selector {
//            val new = get(max)
//            val heap = maxHeap.peek()
//            println("MAX: $new; HEAP: $heap")
//        }

        var i = 0
        var j = 0

        selector {
            val e = get(added)
            // don't update if this object is null
            if (e is NullGraphable) return@selector null

//            skipLists.forEach { (key, list) ->
//                if (i++ % (key - 1) == 0) {
//                    println("Add[$key] $e")
//                    list.add(e)
//                }
//            }

            maxHeap.add(e)
            minHeap.add(e)
            updateHeaps()
//            println("ADDED: ${e}; HEAP: ${minHeap.peek()?.bottom}, ${maxHeap.peek()?.top}")
        }

        selector {
            val e = get(removed)
            // don't update if this object is null
            if (e is NullGraphable) return@selector null

            // skipList removal is handled by the FifoQueue
//            skipLists.forEach { (key, list) ->
//                if (j++ % (key - 1) == 0) {
//                    println("Del[$key] $e")
//                    list.remove(e)
//                }
//            }

            maxHeap.remove(e)
            minHeap.remove(e)
            updateHeaps()
//            println("REMOVED: ${e.let { "(${it.bottom}, ${it.top})"}}; HEAP: ${minHeap.peek()?.bottom}, ${maxHeap.peek()?.top}")
        }
    }

    var isCandleGraph = selector {
        val e = get(end)
        return@selector e is CandleGraphable
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun updateHeaps() {
        min.value = minHeap.peek()?.bottom ?: 0f
        max.value = maxHeap.peek()?.top ?: 0f
//        println("HEAP_UPDATE: ${min.value}, ${max.value}")
    }

    override fun clear() {
        maxHeap.clear()
        minHeap.clear()
        min.value = initialValue.bottom
        max.value = initialValue.top
        super.clear()
    }
}