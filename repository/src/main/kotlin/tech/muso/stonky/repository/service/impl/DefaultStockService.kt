package tech.muso.stonky.repository.service.impl

import tech.muso.stonky.repository.controller.StockController
import tech.muso.stonky.repository.model.Stock
import tech.muso.stonky.repository.repository.StockRepository
import tech.muso.stonky.repository.service.StockService
import tech.muso.stonky.repository.service.exception.StockNotFoundException
import org.springframework.stereotype.Service

@Service
class DefaultStockService(val stockRepository: StockRepository) : StockService {

    override fun getStock(id: String): Stock = stockRepository.findById(id).orElseThrow {
        StockNotFoundException("Unable to find Stock for $id id")
    }

    override fun getAllStocks(): List<Stock> = stockRepository.findAll().toList()

    override fun updateStock(id: String, stockDto: StockController.StockDto): Stock {
        val stock: Stock = stockRepository.findById(id).orElseThrow { StockNotFoundException("Unable to find Stock for $id id") }
        val updatedStock = stock.copy(symbol = stockDto.name.orEmpty(), name = stockDto.genre.orEmpty(), year = stockDto.year)
        updatedStock.id = stock.id
        return stockRepository.save(updatedStock)
    }

    override fun updateStock(stock: Stock): Stock = stockRepository.save(stock)

    override fun createStock(stockDto: StockController.StockDto): Stock =
        stockRepository.save(
            Stock(
                symbol = stockDto.name.orEmpty(),
                name = stockDto.genre.orEmpty(),
                year = stockDto.year
            )
        )

    override fun deleteStock(id: String) = stockRepository.delete(getStock(id))
}