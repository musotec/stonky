package tech.muso.stonky.endpoints

import com.studerw.tda.model.quote.EquityQuote
import com.studerw.tda.model.quote.EtfQuote
import com.studerw.tda.model.quote.FutureQuote
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import tech.muso.stonky.CommandType
import tech.muso.stonky.Parser
import tech.muso.stonky.repository.TdaRepository
import tech.muso.stonky.config.Config


fun Route.routeWebSocketQuotes() {
    webSocket(Config.server.endpoint + Quote.path) {
        send(Frame.Text("CONNECTED TO STONKY SERVER"))

        // get initial frame
        val frame = incoming.receive()
        val command = Parser.parseCommand(frame)

        when (command.type) {
            CommandType.CANDLE_REPLAY -> {
                val symbol = command.symbol

                val tdaClient = TdaRepository.getInstance()

                while(true) {
                    val clientResponseFrame = incoming.receive()
                    if (clientResponseFrame is Frame.Text) {

                        val text = clientResponseFrame.readText()

                        // after we exhaust all the candles, start outputting live
                        val quote = tdaClient.fetchQuote(symbol)
                        val price = (quote as? EtfQuote)?.lastPrice ?: (quote as? EquityQuote)?.lastPrice ?: (quote as? FutureQuote)?.lastPriceInDouble
                        val time = (quote as? EtfQuote)?.quoteTimeInLong ?: (quote as? EquityQuote)?.quoteTimeInLong ?: System.currentTimeMillis()

                        println(quote)

                        send(
                            Frame.Text(
                                price.toString() + ":" + time
                            )
                        )

                        // debug close
                        if (text == "CLOSE") {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }

                        // rate limit this by 1 second
                        delay(1000)
                    }
                }
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