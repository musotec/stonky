import Drawings.Companion.toJson
import junit.framework.TestCase
import org.junit.Test

// TODO: this should be refactored to exist in the :core module
class DrawingsTest : TestCase() {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun String.removeWhitespace(): String {
        return replace("\\s".toRegex(), "")
    }

    @Test
    fun testDrawingsJson() {
        val example = """
            {
              "AAPL": [
                {
                  "type":"LINE",
                  "time":1544129220,
                  "points":[172.26,172.3,172.16,172.18],
                  "label":"3892",
                  "color":-1
                }
              ]
            }
        """.trimIndent()
        val symbol = "AAPL"
        val testDrawing = Drawings.Drawing(
            type = "LINE",
            time = 1544129220,
            points = floatArrayOf(172.26f, 172.3f, 172.16f, 172.18f),
            color = "FFFFFFFF".toUInt(16).toInt(),
            label = "3892"
        )

        val testDrawings = Drawings(symbol, testDrawing)
        assertEquals(example.removeWhitespace(), testDrawings.toJson().removeWhitespace())

        // TODO: test multiple symbols
    }

}