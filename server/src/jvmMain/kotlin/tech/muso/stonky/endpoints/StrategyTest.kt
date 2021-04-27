package tech.muso.stonky.endpoints

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import tech.muso.stonky.CommandType
import tech.muso.stonky.Parser
//import tech.muso.stonky.core.model.Bars
//import tech.muso.stonky.core.model.Bars.Companion.toJson
//import tech.muso.stonky.core.model.Candle
import tech.muso.stonky.repository.TdaRepository
import tech.muso.stonky.repository.toCandle
import tech.muso.stonky.studies.ExampleStudy
import java.util.*
import tech.muso.stonky.config.Config

fun Route.routeWebSocketTester() {
    webSocket(Config.server.endpoint + "/tester") { // TODO: refactor this path
        send(Frame.Text("CONNECTED TO STONKY SERVER"))

        // get initial frame
        val frame = incoming.receive()
        val command = Parser.parseCommand(frame)

        when (command.type) {
            CommandType.CANDLE_REPLAY -> {
                val symbol = command.symbol

                val tdaClient = TdaRepository.getInstance()
                val priceHistory = tdaClient.priceHistory(symbol)
                var currentCandle = Candle.NULL_CANDLE

                // CREATE STRATEGY TESTER

                // need to store the candles as we get them and on each new one, execute study processing
                // study will directly send a response over the current session
                val study = ExampleStudy(this, symbol)

                priceHistory.candles.forEach {
                    val clientResponseFrame = incoming.receive()
                    if (clientResponseFrame is Frame.Text) {

                        val text = clientResponseFrame.readText()
                        println("Client said: $text")
                        currentCandle = it.toCandle()
                        // add to our study
                        study.add(currentCandle)

                        // send the price data
//                        send(Frame.Text(bars.toJson()))

                        // then send the response
                        study.execute()

                        // debug close
                        if (text == "CLOSE") {
                            return@webSocket
                        }
                    }
                }
            }
            CommandType.TRADE_UPDATE -> TODO()
            CommandType.CANDLE_SIMULATE -> TODO()
            CommandType.CANDLE_LIVE_REPLAY -> TODO()
        }
    }
}