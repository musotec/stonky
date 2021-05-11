import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.routing.route
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.event.Level
import tech.muso.stonky.endpoints.routePortfolios
import tech.muso.stonky.endpoints.routeWebSocketPortfolios
import tech.muso.stonky.endpoints.routeWebSocketPriceHistory
import java.time.Duration
import tech.muso.stonky.config.Config

val stockWatchlist = mutableListOf(
    WatchListItem("AAPL", 1),
    WatchListItem("MSFT", 2),
    WatchListItem("SPY", 3),
    WatchListItem("QQQ", 3)
)

fun main() {

    // start a Netty server with our configuration
    embeddedServer(Netty, port = Config.server.port) {

        install(ContentNegotiation) {
            json()
        }

        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Delete)
            anyHost()
        }

        val client = HttpClient(CIO) {
//            install(Logging)
        }

        install(Compression) {
            gzip()
        }

        install(io.ktor.websocket.WebSockets) {
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

            get("/") {
                call.respondText(
                    this::class.java.classLoader?.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }

            static("/") {
                resources("")
            }

            get("/hello") {
                call.respondText("Hello, API!")
            }

            this.routePortfolios()

            route(WatchListItem.path) {
                get {
                    call.respond(stockWatchlist)
                }
                post {
                    stockWatchlist += call.receive<WatchListItem>()
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
                    stockWatchlist.removeIf { it.id == id }
                    call.respond(HttpStatusCode.OK)
                }
            }

            routeWebSocketPriceHistory(client)
//            routeWebSocketQuotes()
            routeWebSocketPortfolios()
//            routeWebSocketTester()
        }
    }.start(wait=true)
}