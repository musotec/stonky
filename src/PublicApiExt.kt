import tech.muso.stonky.core.model.Candle

/**
 * Convert from TD Ameritrade (v2) PriceHistory Candle to our Candle
 */
fun com.studerw.tda.model.history.Candle.toCandle(): Candle {
    return Candle(
        timeSeconds = (datetime / 1000).toInt(),
        close = close.toDouble(),
        high = high.toDouble(),
        low = low.toDouble(),
        open = open.toDouble(),
        volume = volume.toInt()
    )
}
