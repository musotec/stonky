import junit.framework.TestCase
import org.junit.Test
import tech.muso.stonky.common.PortfolioSlice
import tech.muso.stonky.common.PortfolioSlice.Companion.toJson

class PortfolioSliceTest : TestCase() {

    @Test
    fun testIterator() {
        val portfolio = PortfolioSlice(0, 1, "parent0", System.currentTimeMillis())
        val portfolio1 = PortfolioSlice(1, 1, "child1", System.currentTimeMillis())
        val portfolio2 = PortfolioSlice(2, 1, "child2", System.currentTimeMillis())
        val portfolio3 = PortfolioSlice(3, 1, "child3", System.currentTimeMillis())

        portfolio.add(portfolio1)
        portfolio.add(portfolio2)
        portfolio.add(portfolio3)

        var j = 0
        portfolio.forEachIndexed { i, child ->
            assertEquals("child${i+1}", child.name)
            j++
        }

        assertEquals(3, j)
    }

    /** Test multiple calls to child set creates a linked list of children */
    @Test
    fun testChildNodesLinked() {
        val portfolio = PortfolioSlice(0, 1, "parent0", System.currentTimeMillis())
        val portfolio1 = PortfolioSlice(1, 1, "child1", System.currentTimeMillis())
        val portfolio2 = PortfolioSlice(2, 1, "child2", System.currentTimeMillis())
        val portfolio3 = PortfolioSlice(3, 1, "child3", System.currentTimeMillis())

        portfolio.add(portfolio1)
        portfolio.add(portfolio2)
        portfolio.add(portfolio3)

//        assertEquals("child1", portfolio.child?.name) // NOTE: child is not exposed.

        assertEquals("child1", portfolio.first().name)
        assertEquals("child2", portfolio1.next?.name)
        assertEquals("child3", portfolio2.next?.name)
    }

    @Test
    fun testChildNodesHaveParentSet() {
        val portfolio = PortfolioSlice(0, 1, "parent0", System.currentTimeMillis())
        val portfolio1 = PortfolioSlice(1, 1, "child1", System.currentTimeMillis())
        val portfolio2 = PortfolioSlice(2, 1, "child2", System.currentTimeMillis())

        portfolio.add(portfolio1)
        assertEquals("parent0", portfolio1.parent.name)

        portfolio.add(portfolio2)
        assertEquals("parent0", portfolio2.parent.name)
    }

    @Test
    fun testChildNodeRemoval() {
        val portfolio = PortfolioSlice(0, 1, "parent0", System.currentTimeMillis())
        val child1 = PortfolioSlice(1, 1, "child1", System.currentTimeMillis())
        val child2 = PortfolioSlice(2, 1, "child2", System.currentTimeMillis())
        val child3 = PortfolioSlice(3, 1, "child3", System.currentTimeMillis())

        portfolio.add(child1)
        portfolio.add(child2)
        portfolio.add(child3)

        assertEquals("child2", child1.next?.name)
        assertEquals("child3", child2.next?.name)

        // test middle node removal
        portfolio.remove(child2)
        assertEquals("child3", child1.next?.name)

        // test head node removal
        portfolio.add(child2)
        portfolio.remove(child1)
        assertEquals("child3", portfolio.first().name)

        // test tail node removal
        assertEquals("child2", portfolio.last().name)   // assert we have correct tail
        portfolio.remove(child2)   // remove tail node
        assertEquals("child3", portfolio.last().name)   // assert child3 is new tail

        // test all children removed
        portfolio.remove(child3)
        assertEquals(true, portfolio.isEmpty())
    }


    @Test
    fun testJson() {
        val portfolio = PortfolioSlice(0, 1, "parent0", System.currentTimeMillis())
        portfolio.parent = portfolio

        val portfolio1 = PortfolioSlice(1, 1, "child1", System.currentTimeMillis())
        val portfolio2 = PortfolioSlice(2, 1, "child2", System.currentTimeMillis())
        val portfolio3 = PortfolioSlice(3, 1, "child3", System.currentTimeMillis())
        portfolio.add(portfolio1)
        portfolio.add(portfolio2)
        portfolio.add(portfolio3)

        portfolio1.type = "testType"

        println(portfolio.toJson())

        val p = PortfolioSlice.fromJson(portfolio.toJson())

        assertEquals(portfolio.toJson(), p.toJson())
    }

    @Test
    fun testServerResponse() {

        val portfolio = PortfolioSlice.newInstance()

        portfolio.add(
            PortfolioSlice(
                id = portfolio.id + 1,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                version = 0,
                name = "SPY",
                time = System.currentTimeMillis()
            ).apply {
                amount = 1.0
                weight = 0.25f
            }
        )

        portfolio.add(
            PortfolioSlice(
                id = portfolio.id + 2,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                version = 0,
                name = "QQQ",
                time = System.currentTimeMillis()
            ).apply {
                amount = 1.0
                weight = 0.25f
            }
        )

        val usd =
            PortfolioSlice(
                id = portfolio.id + 3,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                version = 0,
                name = "USD",
                time = System.currentTimeMillis()
            ).apply {
                amount = 700.0
                weight = 0.5f
            }

        portfolio.add(usd)

        println(portfolio.toList())
        println(portfolio.toJson())

        // TODO; use fake repo in test for price obtain
        assertEquals(1450.0, portfolio.marketValue)
        assertEquals(751.0, portfolio.getMarketPrice())
        assertEquals(375.0, usd.weightedAmount) // expect 375.5 if fractional allowed

//        val read = PortfolioSlice.fromJson(portfolio.toJson())
//        read.forEach {
//            println("read ${it.toDisplayString()}")
//        }
    }

    @Test
    fun testNestedPortfolios() {

        val portfolio = PortfolioSlice.newInstance()
        val nested = PortfolioSlice.newInstance()

        portfolio.add(
            PortfolioSlice(
                id = portfolio.id + 1,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                version = 0,
                name = "SPY",
                time = System.currentTimeMillis()
            ).apply {
                amount = 1.0
                weight = 0.25f
            }
        )

        portfolio.add(
            PortfolioSlice(
                id = portfolio.id + 2,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                version = 0,
                name = "QQQ",
                time = System.currentTimeMillis()
            ).apply {
                amount = 1.0
                weight = 0.25f
            }
        )

        portfolio.add(
            PortfolioSlice(
                id = portfolio.id + 3,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                version = 0,
                name = "USD",
                time = System.currentTimeMillis()
            ).apply {
                amount = 700.0
                weight = 0.3f
            }
        )

        portfolio.add(nested.apply {
            name = "GLD/SLV"
            amount = 1.0
            weight = 0.2f
        })

        nested.add(
            PortfolioSlice(
                id = nested.id + 1,
                version = 1,
                name = "GLD",
                time = System.currentTimeMillis()
            ).apply {
                amount = -1.0
                weight = (25f/150f)
            }
        )

        nested.add(
            PortfolioSlice(
                id = nested.id + 2,
                version = 1,
                name="SLV",
                time = System.currentTimeMillis()
            ).apply {
                amount = 6.0
                weight = (125f/150f)
            }
        )

        println(portfolio.toList())
        println(portfolio.toJson())
    }

    @Test
    fun testCommit() {
//        TODO()
    }
}


