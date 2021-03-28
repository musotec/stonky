package tech.muso.stonky

import Config
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
import io.ktor.html.*
import org.slf4j.event.*
import tech.muso.stonky.core.model.Bars
import tech.muso.stonky.core.model.Bars.Companion.toJson
import tech.muso.stonky.core.model.Candle
import toCandle
import java.util.*

import io.ktor.http.content.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import java.io.*

fun main() {
    val config = Config.defaultConfig()
    // start a Netty server with our configuration
    embeddedServer(Netty, port = config.server.port) {
        val client = HttpClient(CIO) {
            install(Logging) {
                level = LogLevel.HEADERS
            }
        }

        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(150)
            timeout = Duration.ofSeconds(150)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        install(CallLogging) {
            level = Level.INFO
            filter { call -> call.request.path().startsWith("/") }
        }

        install(DefaultHeaders)

        routing {
            routeFilesystem()
            routeResources()
            routeWebSocket(config)
        }
    }.start(wait=true)
}

fun Route.routeFilesystem() {
    get("/") {
        call.respondHtml {
            head {
                title { +"Ktor: static-content" }
                styleLink("/static/cschart.css")
                script(src = "/static/d3.v3.min.js") {}
                script(src = "/static/d3-queue.v3.min.js") {}
                script(src = "/static/topojson.v1.min.js") {}
                script(src = "/static/cschart.js") {}
                script(src = "/static/csbars.js") {}
                script(src = "/static/csheader.js") {}
                script(src = "/static/csdataprep.js") {}
                script(src = "/static/csmain.js") {}
            }
            body {
                div { id="demobox"
                    div { id="csbox"
                        div { id="option"
                            input(InputType.button) {
                                id="oneM"; name="1M"; value="1M"
                            }
                            input(InputType.button) {
                                id="threeM"; name="3M"; value="3M"
                            }
                            input(InputType.button) {
                                id="sixM"; name="6M"; value="6M"
                            }
                            input(InputType.button) {
                                id="oneY"; name="1Y"; value="1Y"
                            }
                            input(InputType.button) {
                                id="twoY"; name="2Y"; value="2Y"
                            }
                            input(InputType.button) {
                                id="fourY"; name="4Y"; value="4Y"
                            }
                        }
                        div { id="infobar"
                            div(classes = "infohead") { id="infodate" }
                            div(classes = "infobox") { id="infoopen" }
                            div(classes = "infobox") { id="infohigh" }
                            div(classes = "infobox") { id="infolow" }
                            div(classes = "infobox") { id="infoclose" }
                        }
                        div { id="chart1" }
                    }

                }
            }
        }
    }

    static("static") {
        // When running under IDEA make sure that working directory is set to this sample's project folder
        staticRootFolder = File("web")
        files("css")
        files("js")
        file("image.png")
        file("random.txt", "image.png")
        file("stockdata.csv")
        default("index.html")
    }

    static("custom") {
        staticRootFolder = File("/tmp") // Establishes a root folder
        files("public") // For this to work, make sure you have /tmp/public on your system
        static("themes") {
            // services /custom/themes
            files("data")
        }
    }
}

fun Route.routeResources() {
    get("/resources") {
        call.respondHtml {
            head {
                title { +"Ktor: static-content" }
                styleLink("/static-resources/styles.css")
            }
            body {
                p {
                    +"Hello from Ktor static content served from resources, if the background is cornflowerblue"
                }
            }
        }
    }

    static("static-resources") {
        resources("css")
    }
}

fun Route.routeWebSocket(config: Config) {
    webSocket(config.server.endpoint) {
        send(Frame.Text("CONNECTED TO STONKY SERVER"))

        // get initial frame
        val frame = incoming.receive()
        val command = Parser.parseCommand(frame)

        when (command.type) {
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
