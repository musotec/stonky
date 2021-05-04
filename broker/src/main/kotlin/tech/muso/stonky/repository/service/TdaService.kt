package tech.muso.stonky.repository.service

import Bars
import Candle
import tech.muso.stonky.repository.service.exception.TradeNotFoundException

interface TdaService {

    @Throws(TradeNotFoundException::class)
    fun cacheCandles(table: String, candles: List<Candle>, offset: Long): Boolean?
    fun getCachedCandles(symbol: String, table: String, offset: Long, startTimeEpochSeconds: Long): Bars
    fun getCandles(symbol: String, startTimeEpochSeconds: Long?): Bars
}