package tech.muso.stonky.repository.service

import Trade
import TradeSet
import tech.muso.stonky.repository.service.exception.TradeNotFoundException

interface AlpacaService {

    @Throws(TradeNotFoundException::class)
    fun cacheTrades(table: String, trades: List<Trade>, offset: Long): Boolean?
    fun getCachedTrades(symbol: String, table: String, offset: Long, startTimeEpochSeconds: Long): TradeSet
    fun getTrades(symbol: String, startTimeEpochSeconds: Long): TradeSet
//    fun getMoreTrades(symbol: String, startTimeEpochSeconds: Long, nextPageToken: String): TradeSet
}