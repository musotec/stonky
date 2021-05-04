package tech.muso.stonky.endpoints

import Bars
import Candle
import Bars.Companion.toJson
import com.studerw.tda.model.history.FrequencyType
import com.studerw.tda.model.history.PriceHistReq
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import tech.muso.stonky.CommandType
import tech.muso.stonky.Parser
import tech.muso.stonky.repository.TdaRepository
import tech.muso.stonky.repository.toCandle
import tech.muso.stonky.config.Config

fun Route.routeWebSocketPriceHistory() {
    webSocket(Config.server.endpoint + Bars.path) {
        send(Frame.Text("CONNECTED TO STONKY SERVER"))

        // get initial frame
        val frame = incoming.receive()
        val command = Parser.parseCommand(frame)

        when (command.type) {
            CommandType.CANDLE_REPLAY -> {
//                val symbol = command.symbol
//
//                val result = TdaControllerApi().getPriceHistoryUsingGET(symbol, System.currentTimeMillis()/1000).candles
//                println("$result")
//                result.forEach {
//                    val clientResponseFrame = incoming.receive()
//                    if (clientResponseFrame is Frame.Text) {
//                        val text = clientResponseFrame.readText()
//
//                        println("Client said: $text [$it]")
//
//                       send(
//                            Frame.Text(
//                                Bars(symbol, Candle(
//                                    timeSeconds = it.timeSeconds,
//                                    open = it.open,
//                                    close = it.close,
//                                    high = it.high,
//                                    low = it.low,
//                                    volume = it.volume
//                                )).toJson()
//                            )
//                        )
//
//                        // debug close
//                        if (text == "CLOSE") {
//                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
//                        }
//                    }
//                }
            }
            CommandType.CANDLE_SIMULATE -> {
//                val symbol = "MOCK"
//                var currentCandle = Candle(
//                    open = 100.0,
//                    close = 100.0,
//                    high = 100.0,
//                    low = 100.0,
//                )
//                val bars = Bars(mutableMapOf(Pair(symbol, currentCandle.toCandleList())))
//
//                while (true) {
//                    val clientResponseFrame = incoming.receive()
//                    currentCandle = currentCandle.next()
//                    if (clientResponseFrame is Frame.Text) {
//                        val text = clientResponseFrame.readText()
//                        println("Client said: $text")
//                        bars[symbol] = currentCandle.toCandleList()
//                        send(Frame.Text(bars.toJson()))
//
//                        // debug close
//                        if (text == "CLOSE") {
//                            return@webSocket
//                        }
//                    }
//                }
            }

            CommandType.TRADE_UPDATE -> TODO()
            CommandType.CANDLE_LIVE_REPLAY -> TODO()
        }
    }
}