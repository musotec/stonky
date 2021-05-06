package tech.muso.stonky.repository.service

import tech.muso.stonky.repository.controller.TradeController
import tech.muso.stonky.repository.model.TestTrade
import tech.muso.stonky.repository.model.Stock
import tech.muso.stonky.repository.service.exception.TradeNotFoundException
import tech.muso.stonky.repository.service.exception.StockNotFoundException

interface TradeService {

    @Throws(TradeNotFoundException::class)
    fun getTrade(id: String): TestTrade

    fun getAllTrades(): List<TestTrade>

    @Throws(TradeNotFoundException::class)
    fun updateTrade(id: String, tradeDto: TradeController.TradeDto): TestTrade

    fun createTrade(tradeDto: TradeController.TradeDto): TestTrade

    @Throws(TradeNotFoundException::class)
    fun deleteTrade(id: String)

    @Throws(TradeNotFoundException::class, StockNotFoundException::class)
    fun addTradeToStock(TradeId: String, stockId: String): Stock
}