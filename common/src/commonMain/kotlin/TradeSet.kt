import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TradeSet(
    val symbol: String,
    val trades: List<Trade> = mutableListOf(),
    val cached: Boolean = false,
    @SerialName("next_page_token") val nextPageToken: String? = null    // TODO: remove this if not needed.
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TradeSet

        if (symbol != other.symbol) return false
        trades.zip(other.trades) { a, b -> if (a != b) return false }
        return true
    }

    @Serializable   // TODO: use annotation to specify limit range (1-10_000)
    data class Request(
        val symbol: String,
        val startTime: Long,
        val endTime: Long,
        val limit: Int = 1000,
        val page_token: String? = null
    )

    constructor(symbol: String, trade: Trade) : this(symbol, listOf(trade))

    companion object {
        fun TradeSet.toJson(): String = Json.encodeToString(serializer(), this)
        fun fromJson(string: String): TradeSet = Json.decodeFromString(serializer(), string)

        const val path = "/trades"
    }
}