package tech.muso.stonky.repository.service.impl

import tech.muso.stonky.repository.controller.TradeController
import tech.muso.stonky.repository.model.Trade
import tech.muso.stonky.repository.model.Stock
import tech.muso.stonky.repository.repository.TradeRepository
import tech.muso.stonky.repository.service.TradeService
import tech.muso.stonky.repository.service.StockService
import tech.muso.stonky.repository.service.exception.StockNotFoundException
import org.springframework.stereotype.Service
import tech.muso.stonky.repository.producer.TradeAddedProducer
import java.util.*

@Service
class DefaultTradeService(
    val tradeRepository: TradeRepository,
    val stockService: StockService,
    val tradeAddedProducer: TradeAddedProducer
) : TradeService {

    override fun getTrade(id: String) = tradeRepository.findById(id).orElseThrow {
        StockNotFoundException("Unable to find Stock for $id id")
    }

    override fun getAllTrades(): List<Trade> = tradeRepository.findAll().toList()

    override fun updateTrade(id: String, tradeDto: TradeController.TradeDto): Trade {
        val Trade = getTrade(id).copy(tradeDto.foo, tradeDto.bar, tradeDto.date)
        Trade.id = id
        return tradeRepository.save(Trade)
    }

    override fun createTrade(tradeDto: TradeController.TradeDto): Trade {
        // TODO: when trade is created, have the trade published.
//        tradeAddedProducer.publish(tradeDto.stock)
        return tradeRepository.save(Trade(tradeDto.foo, tradeDto.bar, tradeDto.date))
    }

    override fun deleteTrade(id: String) = tradeRepository.deleteById(id)

    override fun addTradeToStock(TradeId: String, StockId: String): Stock {
        val stock: Stock = stockService.getStock(StockId)
        val trade: Trade = getTrade(TradeId)
        (stock.trades as ArrayList).add(trade)
        return stockService.updateStock(stock)
    }
}