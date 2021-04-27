import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

@Serializable
data class Candle(
    @SerialName("t") val timeSeconds: Int,
    @SerialName("o") val open: Double,
    @SerialName("h") val high: Double,
    @SerialName("l") val low: Double,
    @SerialName("c") val close: Double,
    @SerialName("v") val volume: Int
) {
    companion object {
        val NULL_CANDLE = Candle(-1, 0.0, 0.0, 0.0, 0.0, -1)
        fun Candle.isNull() = timeSeconds == -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Candle

        if (timeSeconds != other.timeSeconds) return false
        if (open != other.open) return false
        if (high != other.high) return false
        if (low != other.low) return false
        if (close != other.close) return false
        if (volume != other.volume) return false

        return true
    }

    /**
     * Make a new Candle list.
     */
    fun toCandleList(): List<Candle> {
        return listOf(this)
    }

    /**
     * Update the current candle with the given price and time.
     * @return a list of candles, making a new candle if the time is past the threshold.
     */
    fun update(price: Double, timeSeconds: Int): List<Candle> {
        val high = max(this.high, price)
        val low = min(this.low, price)
        val threshold = 1 // TODO: determine the movement of these
        if (timeSeconds - this.timeSeconds > threshold) {
            return listOf(
                this,
                Candle(
                    timeSeconds,
                    open=price,
                    high=price,
                    low=price,
                    close=price,
                    volume=volume
                )
            )
        } else {
            return Candle(
                this.timeSeconds,
                open,
                high,
                low,
                price,
                volume  // FIXME: volume is not accounted for
            ).toCandleList()
        }

    }
}
