import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.max
import kotlin.math.min

@Serializable
data class Trade(
    @SerialName("t") val timestamp: Long,
    @Transient val nano: Int = 0,  // @SerialName("n")
    @SerialName("x") val exchange: String,
    @SerialName("p") val price: Double,
    @SerialName("s") val size: Int,
    @SerialName("c") val conditions: ArrayList<String>, // TODO: can byte pack to an int.
    @SerialName("i") val id: Long,
    @SerialName("z") val tape: String
) {
//    companion object {
//        val NULL_CANDLE = Trade(-1, 0.0, 0.0, 0.0, 0.0, -1)
//        fun Trade.isNull() = timeSeconds == -1
//    }

//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null || this::class != other::class) return false
//
//        other as Trade
//
//        if (timeSeconds != other.timeSeconds) return false
//        if (open != other.open) return false
//        if (high != other.high) return false
//        if (low != other.low) return false
//        if (close != other.close) return false
//        if (volume != other.volume) return false
//
//        return true
//    }

    fun toList(): List<Trade> = listOf(this)
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null || this::class != other::class) return false
//
//        other as Trade
//
//        if (timestamp != other.timestamp) return false
//        if (exchange != other.exchange) return false
//        if (price != other.price) return false
//        if (size != other.size) return false
//        if (!conditions.contentEquals(other.conditions)) return false
//        if (id != other.id) return false
//        if (tape != other.tape) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = timestamp.hashCode()
//        result = 31 * result + exchange.hashCode()
//        result = 31 * result + price.hashCode()
//        result = 31 * result + size
//        result = 31 * result + conditions.contentHashCode()
//        result = 31 * result + id.hashCode()
//        result = 31 * result + tape.hashCode()
//        return result
//    }
}
