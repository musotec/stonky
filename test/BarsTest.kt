import junit.framework.TestCase
import org.junit.Test
import tech.muso.stonky.core.model.Bars
import tech.muso.stonky.core.model.Bars.Companion.toJson
import tech.muso.stonky.core.model.Candle

// TODO: this should be refactored to exist in the :core module
class BarsTest : TestCase() {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun String.removeWhitespace(): String {
        return replace("\\s".toRegex(), "")
    }

    @Test
    fun testBarsJson() {
        val example = """
            {
              "AAPL": [
                {
                  "t": 1544129220,
                  "o": 172.26,
                  "h": 172.3,
                  "l": 172.16,
                  "c": 172.18,
                  "v": 3892
                }
              ]
            }
        """.trimIndent()
        val symbol = "AAPL"
        val testCandle = Candle(
            timeSeconds = 1544129220,
            open = 172.26,
            high = 172.3,
            low = 172.16,
            close = 172.18,
            volume = 3892
        )

        val testBars = Bars(mutableMapOf(Pair(symbol, listOf(testCandle))))
        assertEquals(example.removeWhitespace(), testBars.toJson().removeWhitespace())

        // TODO: test multiple symbols
    }

}