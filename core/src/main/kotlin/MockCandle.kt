package tech.muso.stonky.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Random

/**
 * Create a Mock Candle
 */
@JsonClass(generateAdapter = true)
data class MockCandle(
    @Json(name="t") override val timeSeconds: Int = (System.currentTimeMillis() / 1000).toInt(),
    @Json(name="o") override val open: Double,
    @Json(name="h") override val high: Double,
    @Json(name="l") override val low: Double,
    @Json(name="c") override val close: Double,
    @Json(name="v") override val volume: Int = MIN_VOLUME
) : AbstractCandle() {

    companion object {
        val random = Random(1234567890L)
        const val SIMULATED_TRADES_PER_CANDLE = 100
        const val SIMULATED_VOLATILITY = 1.25
        const val MAX_VOLUME = 1000
        const val MIN_VOLUME = 500
        const val CANDLE_DELTA = 1000 * 1000  // every 1000ms (1s)
    }

    /**
     * Generate the next double following a normal distribution.
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline fun gen(prev: Double): Double {
        return prev + (random.nextGaussian() * SIMULATED_VOLATILITY)
    }

    /**
     * Generate the next candle randomly from the current one.
     */
    fun next(): MockCandle {
        // take previous close value and simulate a Normal distribution
        // for 100 ticks to get the next candle values
        val start = this.close
        val nextOpen: Double = gen(start)
        var nextHigh: Double = nextOpen
        var nextLow: Double = nextOpen
        var prev = nextOpen
        for (i in 1 until SIMULATED_TRADES_PER_CANDLE) {
            val next = gen(prev)
            // update min/max
            if (nextLow > next) {
                nextLow = next
            } else if (nextHigh < next) {
                nextHigh = next
            }
            prev = next
        }

        return MockCandle(
            open = nextOpen,
            close = prev,
            high = nextHigh,
            low = nextLow,
            volume = random.nextInt(MAX_VOLUME - MIN_VOLUME) + MIN_VOLUME,
            timeSeconds = timeSeconds + CANDLE_DELTA
        )
    }
}
