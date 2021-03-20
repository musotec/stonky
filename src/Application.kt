package tech.muso.stonky

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
        get("/") {
            call.respondText("HELLO WORLD... you want http://127.0.0.1:${Config.PORT}${Config.PATH_TEST_API} !", contentType = ContentType.Text.Plain)
        }

        webSocket(Config.PATH_TEST_API) {
            var currentCandle = MockCandle(
                open = 100.0,
                close = 100.0,
                high = 100.0,
                low = 100.0,
            )

            send(Frame.Text("SERVER SIMULATING..."))

            val symbol = "MOCK"
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
                        break
                    }
                }
            }
        }
    }
}

