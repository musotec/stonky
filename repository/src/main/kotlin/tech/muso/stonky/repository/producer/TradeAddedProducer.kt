package tech.muso.stonky.repository.producer

import tech.muso.stonky.repository.model.Stock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component

@Component
class TradeAddedProducer(val template: RedisTemplate<String, Any>, val channelTopic: ChannelTopic) {

    companion object {
        val logger : Logger = LoggerFactory.getLogger(TradeAddedProducer::class.java)
    }

    fun publish(stock: Stock) {
        logger.info("Notifying subscribers on adding a new Trade {} {}", stock.id, stock.symbol)
        template.convertAndSend(channelTopic.topic, stock)
    }
}