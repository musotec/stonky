package tech.muso.stonky.studies

import Candle
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
//import tech.muso.stonky.core.model.Candle
//import tech.muso.stonky.core.model.Drawing
//import tech.muso.stonky.core.model.Drawings
//import tech.muso.stonky.core.model.Drawings.Companion.toJson
import java.util.*

class ExampleStudy(private val session: DefaultWebSocketServerSession, val symbol: String) {

//    private val studyDrawings = Drawings(mutableMapOf(Pair(symbol, listOf(Drawing.NULL_DRAWING))))

    // nearest dollars as Ints
    private var highestDollarPrice = Int.MIN_VALUE
    private var lowestDollarPrice = Int.MAX_VALUE

    private val candleList = LinkedList<Candle>()
//    private val marks = mutableListOf<Drawing>()

    fun add(currentCandle: Candle) {
        candleList.add(currentCandle)
    }

    suspend fun execute() {
        val c = candleList.last

        // new max
        if (c.high.toInt() > highestDollarPrice) {
            highestDollarPrice = c.high.toInt()
            val y = c.high.toInt().toDouble()
//            marks.add(
//                Drawing(
//                    timeSeconds = c.timeSeconds,
//                    startX = 0.0,
//                    endX = 1.0,
//                    startY = y,
//                    endY = y,
//                    label = (highestDollarPrice).toString()
//                )
//            )
            sendNewResult()
        }

        // new min
        if (c.low.toInt() < lowestDollarPrice) {
            lowestDollarPrice = c.low.toInt()
            val y = c.low.toInt().toDouble()
//            marks.add(
//                Drawing(
//                    timeSeconds = c.timeSeconds,
//                    startX = 0.0,
//                    endX = 1.0,
//                    startY = y,
//                    endY = y,
//                    label = (lowestDollarPrice).toString()
//                )
//            )
            sendNewResult()
        }
    }

    /** Send a new result to the client only if we should do so from the execute function.*/
    private suspend fun sendNewResult() {
        // reassign the drawings we wish to send.
//        studyDrawings[symbol] = marks
//
//        println("sendNewResults $marks")
//        // now send the result
//        session.send(Frame.Text(studyDrawings.toJson()))
    }

}
