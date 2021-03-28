package tech.muso.stonky

import io.ktor.http.cio.websocket.*

object Parser {
    //- "{symbol}"
    //- "{symbol}:{exchange}"
    //- "{symbol}:{exchange}:{asset_class}"
    //- "{asset_id}"

    fun parseSymbol(text: String) {
        // symbol > exchange > asset_class -[else]-> asset
        val tokens = text.split(":")
        when (tokens.size) {
            3 -> {
                //symbol:exchange:asset_class

            }
            2 -> {
                //symbol:exchange

            }
            1 -> {
                //symbol | asset_id

            }
        }
    }

    /**
     * Use the text of the Frame if it is a valid Frame.Text object.
     */
    private inline fun Frame.useText(lambda: (text: String) -> Unit) {
        if (this is Frame.Text) {
            lambda(readText())
        }
    }

    fun parseCommand(frame: Frame): Command {
        frame.useText {
            return when (it) {
                "MOCK" -> Command(CommandType.CANDLE_SIMULATE, "MOCK")
                else -> Command(CommandType.CANDLE_REPLAY, it)
            }
        }

        return Command(CommandType.CANDLE_SIMULATE, "MOCK")
    }
}