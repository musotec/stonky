package tech.muso.stonky.repository.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import tech.muso.stonky.repository.model.Stock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class TradeAddedListener(
    val objectMapper: ObjectMapper,
//    val subscriptionService: SubscriptionService
) : MessageListener {

    companion object {
        val logger : Logger = LoggerFactory.getLogger(TradeAddedListener::class.java)
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val stock = manualMap(message)
//        subscriptionService.notifySubscribers(stock)
        logger.info("Notified on a new Trade creation {}, {}", stock.id, stock.symbol)
    }

    // Jackson is unable to deserialize empty list of Trades. That's why need to manually map
    // Or not mixing model/entity with DTO
    // FIXME
    fun manualMap(message: Message) : Stock {
        val stockMap = objectMapper.readValue(message.toString(), Map::class.java)
        val stock = Stock(stockMap["name"] as String, stockMap["genre"] as String, stockMap["year"] as Int)
        stock.id = stockMap["id"] as String?
        return stock
    }
}