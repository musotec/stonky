import tech.muso.stonky.core.model.*

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

    fun parseCommand(text: String) {

    }
}