package tech.muso.stonky.endpoints

import io.ktor.application.*
import tech.muso.stonky.common.PortfolioSlice
import tech.muso.stonky.common.PortfolioSlice.Companion.toJson
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import tech.muso.stonky.CommandType
import tech.muso.stonky.Parser
import tech.muso.stonky.config.Config

fun Route.routePortfolios() {
    val portfolio = PortfolioSlice.newInstance()
    val nested = PortfolioSlice.newInstance()

    portfolio.add(
        PortfolioSlice(
            id = portfolio.id + 1,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
            version = 0,
            name = "SPY",
            time = System.currentTimeMillis()
        ).apply {
            amount = 1.0
            weight = 0.25f
        }
    )

    portfolio.add(
        PortfolioSlice(
            id = portfolio.id + 2,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
            version = 0,
            name = "QQQ",
            time = System.currentTimeMillis()
        ).apply {
            amount = 1.0
            weight = 0.25f
        }
    )

    portfolio.add(
        PortfolioSlice(
            id = portfolio.id + 3,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
            version = 0,
            name = "USD",
            time = System.currentTimeMillis()
        ).apply {
            amount = 700.0
            weight = 0.3f
        }
    )

    portfolio.add(nested.apply {
        name = "GLD/SLV"
        amount = 1.0
        weight = 0.2f
    })

    nested.add(
        PortfolioSlice(
            id = nested.id + 1,
            version = 1,
            name = "GLD",
            time = System.currentTimeMillis()
        ).apply {
            amount = -1.0
            weight = (25f/150f)
        }
    )

    nested.add(
        PortfolioSlice(
            id = nested.id + 2,
            version = 1,
            name="SLV",
            time = System.currentTimeMillis()
        ).apply {
            amount = 6.0
            weight = (125f/150f)
        }
    )

    route(PortfolioSlice.path) {
        get {
            call.respond(portfolio)
        }
        post {
//            stockWatchlist += call.receive<WatchListItem>()
//            call.respond(HttpStatusCode.OK)
        }
        delete("/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
//                    stockWatchlist.removeIf { it.id == id }
//            stockWatchlist.removeIf { it?.id == id }
//            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.routeWebSocketPortfolios() {
    webSocket(Config.server.endpoint + PortfolioSlice.path) {
        send(Frame.Text("CONNECTED TO STONKY SERVER"))

        // get initial frame
        val frame = incoming.receive()
        val command = Parser.parseCommand(frame)

        when (command.type) {
            CommandType.CANDLE_REPLAY -> {
                //  TODO: use specific lookup for id and also don't reuse the candle stuff...
                val id = command.symbol

                val portfolio = PortfolioSlice.newInstance()
                val nested = PortfolioSlice.newInstance()

                portfolio.add(
                    PortfolioSlice(
                        id = portfolio.id + 1,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                        version = 0,
                        name = "SPY",
                        time = System.currentTimeMillis()
                    ).apply {
                        amount = 1.0
                        weight = 0.25f
                    }
                )

                portfolio.add(
                    PortfolioSlice(
                        id = portfolio.id + 2,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                        version = 0,
                        name = "QQQ",
                        time = System.currentTimeMillis()
                    ).apply {
                        amount = 1.0
                        weight = 0.25f
                    }
                )

                portfolio.add(
                    PortfolioSlice(
                        id = portfolio.id + 3,        // TODO: special flag for unset ids (not located on server); specially handle ids removed on server but still present locally.
                        version = 0,
                        name = "USD",
                        time = System.currentTimeMillis()
                    ).apply {
                        amount = 700.0
                        weight = 0.3f
                    }
                )

                portfolio.add(nested.apply {
                    name = "GLD/SLV"
                    amount = 1.0
                    weight = 0.2f
                })

                nested.add(
                    PortfolioSlice(
                        id = nested.id + 1,
                        version = 1,
                        name = "GLD",
                        time = System.currentTimeMillis()
                    ).apply {
                        amount = -1.0
                        weight = (25f/150f)
                    }
                )

                nested.add(
                    PortfolioSlice(
                        id = nested.id + 2,
                        version = 1,
                        name="SLV",
                        time = System.currentTimeMillis()
                    ).apply {
                        amount = 6.0
                        weight = (125f/150f)
                    }
                )

                while(true) {
                    val clientResponseFrame = incoming.receive()
                    if (clientResponseFrame is Frame.Text) {

                        send(
                            Frame.Text(portfolio.toJson())
                        )

                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
//                        // debug close
//                        if (text == "CLOSE") {
//                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
//                        }
//
//                        // rate limit this by 1 second
//                        delay(1000)
                    }
                }
            }
        }
    }
}