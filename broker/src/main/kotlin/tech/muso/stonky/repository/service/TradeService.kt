package tech.muso.stonky.repository.service

import tech.muso.stonky.repository.controller.TradeController
import tech.muso.stonky.repository.model.Trade
import tech.muso.stonky.repository.model.Stock
import tech.muso.stonky.repository.service.exception.TradeNotFoundException
import tech.muso.stonky.repository.service.exception.StockNotFoundException

interface TradeService {

    @Throws(TradeNotFoundException::class)
    fun getTrade(id: String): Trade

    fun getAllTrades(): List<Trade>

    @Throws(TradeNotFoundException::class)
    fun updateTrade(id: String, tradeDto: TradeController.TradeDto): Trade

    fun createTrade(tradeDto: TradeController.TradeDto): Trade

    @Throws(TradeNotFoundException::class)
    fun deleteTrade(id: String)

    @Throws(TradeNotFoundException::class, StockNotFoundException::class)
    fun addTradeToStock(TradeId: String, stockId: String): Stock
}