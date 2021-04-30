package tech.muso.stonky.repository.service

import tech.muso.stonky.repository.controller.StockController
import tech.muso.stonky.repository.model.Stock
import tech.muso.stonky.repository.service.exception.StockNotFoundException

interface StockService {

    @Throws(StockNotFoundException::class)
    fun getStock(id: String): Stock

    fun getAllStocks(): List<Stock>

    @Throws(StockNotFoundException::class)
    fun updateStock(id: String, stockDto: StockController.StockDto): Stock

    fun updateStock(stock: Stock): Stock

    fun createStock(stockDto: StockController.StockDto) : Stock

    @Throws(StockNotFoundException::class)
    fun deleteStock(id: String)
}