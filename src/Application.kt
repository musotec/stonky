package tech.muso.stonky

import com.studerw.tda.client.HttpTdaClient
import tech.muso.stonky.core.model.MockCandle
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.client.features.logging.*
import io.ktor.features.*
import org.slf4j.event.*
import tech.muso.stonky.core.model.Bars
import tech.muso.stonky.core.model.Bars.Companion.toJson
import tech.muso.stonky.core.model.Candle
import toCandle
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.tomcat.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    routing {
        val config = Config.defaultConfig()
        println(config)

        get("/") {
            call.respondText("HELLO WORLD... you want http://127.0.0.1:${config.server.port}${config.server.endpoint} !", contentType = ContentType.Text.Plain)
        }

        webSocket(ConfigVars.PATH_TEST_API) {
            send(Frame.Text("SERVER SIMULATING..."))

            // get initial frame
            val frame = incoming.receive()
            val command = Parser.parseCommand(frame)

            when(command.type) {
                CommandType.CANDLE_REPLAY -> {
                    val symbol = command.symbol

                    val tda = config.`api-keys`?.tda
                    if (tda == null) {
                        send(Frame.Text("Setup your config.yaml to with api-keys:tda:refresh-token and api-keys:tda:consumer-key!"))
                        return@webSocket
                    }

                    // TODO: these must be loaded from a JSON/YAML file or something to release a jar.
                    val props = Properties().apply {
                        setProperty("tda.client_id", tda.`consumer-key`) // auth.tmp.txt
                        setProperty("tda.token.refresh", tda.`refresh-token`)
                    }

                    val tdaClient = HttpTdaClient(props)
                    val priceHistory = tdaClient.priceHistory(symbol)
                    var currentCandle = Candle.NULL_CANDLE
                    val bars = Bars(mutableMapOf(Pair(symbol, listOf(currentCandle))))
                    priceHistory.candles.forEach {
                        val frame = incoming.receive()
                        if (frame is Frame.Text) {

                            val text = frame.readText()
                            println("Client said: $text")
                            currentCandle = it.toCandle()
                            bars[symbol] = currentCandle.toCandleList()
                            send(Frame.Text(bars.toJson()))

                            // debug close
                            if (text == "CLOSE") {
                                return@webSocket
                            }
                        }
                    }
                }
                CommandType.CANDLE_SIMULATE -> {
                    val symbol = "MOCK"
                    var currentCandle = MockCandle(
                        open = 100.0,
                        close = 100.0,
                        high = 100.0,
                        low = 100.0,
                    )
                    val bars = Bars(mutableMapOf(Pair(symbol, currentCandle.toCandleList())))

                    while (true) {
                        val frame = incoming.receive()
                        currentCandle = currentCandle.next()
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            println("Client said: $text")
                            bars[symbol] = currentCandle.toCandleList()
                            send(Frame.Text(bars.toJson()))

                            // debug close
                            if (text == "CLOSE") {
                                return@webSocket
                            }
                        }
                    }
                }
            }

        }
    }
}

