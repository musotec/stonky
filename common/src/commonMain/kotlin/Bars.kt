import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class Bars(val symbol: String, val candles: List<Candle> = mutableListOf()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Bars

        if (symbol != other.symbol) return false
        candles.zip(other.candles) { a, b -> if (a != b) return false }
        return true
    }

    @Serializable
    data class Request(val symbol: String, val startTime: Int, val candleMinutes: Int)

    constructor(symbol: String, candle: Candle) : this(symbol, listOf(candle))

    companion object {
        fun Bars.toJson(): String = Json.encodeToString(serializer(), this)
        fun fromJson(string: String): Bars = Json.decodeFromString(serializer(), string)

        const val path = "/pricehistory"
    }
}