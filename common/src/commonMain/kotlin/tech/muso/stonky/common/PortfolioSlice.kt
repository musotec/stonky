package tech.muso.stonky.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.floor

@Suppress("PropertyName")
@Serializable
data class PortfolioSlice(
    val id: Int,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
    val version: Int,
    var name: String,
    val time: Long
) : MutableCollection<PortfolioSlice> {
    init {
        require(name.isNotEmpty()) { "name cannot be empty" }
    }

    companion object {
        fun newInstance(): PortfolioSlice {
            return PortfolioSlice(
                // TODO: generate UUID and time created on the server
                id=0,
                version=0,
                name="New Slice Portfolio",
                time=0
            ).run {
                parent = this
                commit()
            }  // commit publishes to server and returns
        }

        fun PortfolioSlice.toJson(): String = Json.encodeToString(serializer(), this)
        fun fromJson(string: String): PortfolioSlice = Json.decodeFromString(serializer(), string).apply {
            parent = this
            // we also need to recursively update the children's parents because the serializer does not do it.
            updateParentLinks()
        }
        const val path = "/portfolio"
    }

    fun toDisplayString(): String {
        return "$name\n$${marketValue}"
    }

    var amount: Double = 0.0
    var weight: Float = 0f  // TODO: automatically set this to non-zero depending on globally defined strategy.
    var type: String = ""   // TODO: will control rendering and strategy for risk/management

    @Transient
    lateinit var parent: PortfolioSlice
    private fun isRoot() = parent == this
    fun isComplex() = _child != null

    private fun updateParentLinks() {
        this.forEach {
            it.parent = this
            // do nullity check to prevent unnecessary iterator creation.
            if (it._child != null)
                it.updateParentLinks()
        }
    }

    var next: PortfolioSlice? = null
        internal set

    /**
     * Must execute commit in order to push to server/save changes.
     */
    fun commit(): PortfolioSlice {
        // FIXME: child updates MUST increment the version of their parents!


        val updatedPortfolio = PortfolioSlice(
            id, version + 1, name, time
        ).also {
            // preserve root status on commit.
            it.parent = if (this.isRoot()) it else this.parent
            it.amount = this.amount
            it.weight = this.weight

//            TODO: update the internally managed variables. children, amount, etc.
        }

        println("commit: ${updatedPortfolio.toJson()}")

        // TODO: push to server, delay, OR THROW EXCEPTION IF UNABLE TO DO SO!!
        return updatedPortfolio
    }

    @Transient
    private var _size: Int = 0
    override val size: Int
        get() = _size

    @SerialName("child")
    internal var _child: PortfolioSlice? = null   // maybe cache childCount for deep recursive case?
    override fun isEmpty(): Boolean = _child == null

    override fun add(element: PortfolioSlice): Boolean {
        // add if head of list
        if (this._child == null) {
            this._child = element
        } else {
            // otherwise add to the end.
            // TODO : should we use head/tail pointer?. Currently O(n) insertion time.
            //  Saves 1 column on db entries. Could also use different type/table for linking.
            //  With Tail pointer we would need to update 2 rows per insert.
            var curr = _child!!
            while (curr.next != null) {
                curr = curr.next!!
            }
            // add child to end of linked list
            curr.next = element
        }

        element.parent = this

        _size++

        // FIXME: element is always added (check duplicates?)
        return true
    }

    override fun remove(element: PortfolioSlice): Boolean {
        // if we have no children, exit
        if (this._child == null) return false
        // if it matches our child, then update pointer to next node.
        if (this._child!!.id == element.id) {
            val old = this._child
            this._child = this._child!!.next
            old!!.next = null   // remove previous link
            // TODO: send delete/update operation to server.
        } else {
            // otherwise, iterate over the linked list
            var curr = _child
            // iterate until we have curr._next = element
            while (curr != null && curr.next?.id != element.id) {
                curr = curr.next
            }
            if (curr == null) return false    // element not found, so exit

            val old = curr.next
            // remove link, preserve order.
            val newNext = curr.next?.next
            curr.next = newNext
            old!!.next = null
            // TODO: send delete/update operation to server.
        }

        _size--

        return true
    }

    fun flatListAll(): List<PortfolioSlice> {
        val list = mutableListOf<PortfolioSlice>()
        var curr = _child
        while (curr != null) {
            if (curr._child == null) {
                list.add(curr)  // add current if it has no children
            } else {
                list.addAll(curr.flatListAll()) // otherwise go deeper.     // TODO: could use linked list structure to avoid making many lists. but these are small lists.
            }
            curr = curr.next
        }

        return list
    }

    override fun iterator(): MutableIterator<PortfolioSlice> {
        return object : MutableIterator<PortfolioSlice> {
            var curr = _child
            override fun hasNext(): Boolean = curr != null
            override fun next(): PortfolioSlice {
                val next = curr
                curr = curr?.next
                return next!!
            }

            override fun remove() {
                TODO("remove the last returned (curr) node from the set.")
            }
        }
    }

//    val history: // TODO: linked list of previous versions? we have the version indicator so this can be a function -> iterator

//    fun getCost() // TODO: returns the cost in buying power

    fun getPriceHistory(): Nothing = TODO()
    fun rebalance(): Nothing = TODO()

    fun getMarketPrice(): Double =
        if (_child != null) {
            sumByDouble { it.getMarketPrice() }
        } else {
            // TODO: use one shot operation for get price.
            when(name) {
                "USD" -> 1.0
                "SPY" -> 400.0
                "QQQ" -> 350.0
                "GLD" -> 160.0
                "SLV" -> 24.0
                else -> 100.0
            }
        }

    val isShort: Boolean get() = amount < 0
    val currentDayCost: Double get() = avgPrice * amount

    var avgPrice: Double = 0.0

    val profit: Double get() = marketValue - currentDayCost

    val marketValue: Double get() =
        if (isRoot()) {
            sumByDouble { it.marketValue }    // current value of a root portfolio is the recursive value of the children
        } else if (_child != null) {
            sumByDouble { it.marketValue } * amount
        } else {
            getMarketPrice() * amount // otherwise it's the price times the amount held
        }

    val target: Double get() =
//        if (parent._child != null) {}
        if (parent.name.contains("/")) {    // FIXME: use an actual type field for pairs trade detection.
            // sum of all the other children (excluding ours)
            parent.sumOf { if (it != this) it.marketValue else 0.0 }
        } else {
            parent.marketValue * weight   // otherwise use the value  // FIXME: this is A LOT of recursion!
        }

    val absValue: Double get() =    //FIXME
        if (isRoot()) {
            sumByDouble { abs(it.marketValue) }
        } else if (_child != null) {
            sumByDouble { it.marketValue * abs(it.amount) }
        } else {
            marketValue
        }

    val weightedAmount: Double get() =
        floor(parent.getMarketPrice() * weight)

    override fun contains(element: PortfolioSlice): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<PortfolioSlice>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<PortfolioSlice>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<PortfolioSlice>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<PortfolioSlice>): Boolean {
        TODO("Not yet implemented")
    }
}
