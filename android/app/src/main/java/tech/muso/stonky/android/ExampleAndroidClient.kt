package tech.muso.stonky.android

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.muso.stonky.common.getStonkyServerAddress
import tech.muso.stonky.common.getStonkyServerPort
import java.lang.Exception

/**
 * Basic Android WebSocket Client to interface with stonky.server
 */
fun subscribe(symbol: String, path: String): Flow<String> = flow {
    // connect to the server (localhost:8080; maps to 10.0.2.2:8080 inside the android emulator)
    val client = HttpClient(CIO).config { install(WebSockets) }
    client.ws(
        method = HttpMethod.Get,
        host = getStonkyServerAddress(),
        port = getStonkyServerPort(),
        path = "/api/$path")
    { // TODO: load the directory from common.

//            if (path.equals("/api/pricehistory")) {
////                send(Frame.Text("CLOSE"))
//                return@ws
//            }

        send(Frame.Text(symbol)) // todo: process some JSON for real
        // launch asynchronously 1000 WebSocket calls every 1000 ms
        val fetchJob = async {
            var i = 0
            while(true) {
                delay(300)
                send(Frame.Text("Fetching... [${i++}]"))

                // early exit; todo: design and document actual mock api
//                    if (i == 30) {
//                        send(Frame.Text("CLOSE"))
//                    }
            }
        }

        var next: Job? = null
        var j = 0
        for (message in incoming) {
            next?.cancel()

            // ignore future responses that will not print.
            if (message !is Frame.Text) {

            } else {
                val string = message.readText()
                if (!string.startsWith("CONNECTED")) {
                    emit(string)
                }
                println("Server said [${++j}]: $string")
            }

            // create an async watchdog that will close if the channel is ever interrupted somehow...
            next = async {
                delay(10_000)
                incoming.cancel()
            }
        }
    }
}