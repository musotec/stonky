import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.client.features.websocket.*
import io.ktor.client.features.websocket.WebSockets
import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.*

object WebSocketExampleClientApp {

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                // todo show loading
                block()
            } catch (error: Throwable) {
                // todo show error
            } finally {
                // todo stop loading
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {

            val config = Config.defaultConfig()

            // create a basic WebSocket client
            val client = HttpClient(CIO).config { install(WebSockets) }

            // connect to the server
            client.ws(
                method = HttpMethod.Get,
                host = config.server.host,
                port = config.server.port,
                path = config.server.endpoint
            ) {
                println("Client Requesting @ ${call.request.url}")

                send(Frame.Text("SPY")) // todo: process some JSON for real

                // launch asynchronously 1000 WebSocket calls every 1000 ms
                async {
                    for (i in 0 until 1000) {
//                        delay(1000)
                        send(Frame.Text("Fetching... [$i]"))

                        // early exit; todo: design and document actual mock api
                        if (i == 30) {
                            send(Frame.Text("CLOSE"))
                        }
                    }
                }

                for (message in incoming) {
                    // ignore future responses that will not print.
                    if (message !is Frame.Text) {

                    } else {
                        println("Server said: ${message.readText()}")
                    }
                }
            }
        }
    }
}
