package tech.muso.stonky.repository.subscription.service.impl

// TODO: replace with WebFlux compatible subscription when implementing
//import tech.muso.stonky.repository.model.Stock
//import tech.muso.stonky.repository.subscription.model.Subscriber
//import tech.muso.stonky.repository.subscription.service.SubscriptionService
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Service
//import java.io.IOException
//
//@Service
//class DefaultSubscriptionService : SubscriptionService {
//
//    companion object {
//        val logger: Logger = LoggerFactory.getLogger(DefaultSubscriptionService::class.java)
//        val subscribers: MutableSet<Subscriber> = hashSetOf()
//    }
//
//    override fun subscribe(subscriber: Subscriber): Subscriber {
//        subscribers.add(subscriber)
//        return subscriber
//    }
//
//    override fun notifySubscribers(stock: Stock) {
//        try {
//            subscribers.forEach { subscriber ->
//                subscriber.send(stock)
//                subscriber.onError { error ->
//                    logger.info("Seems the subscriber has already dropped out. Remove it from the list")
//                    subscriber.completeWithError(error)
//                    subscribers.remove(subscriber)
//                }
//            }
//        } catch (ioException: IOException) {
//            logger.warn("Failed to notify subscriber about the new Trade = {}, {}, {}", stock.symbol, stock.name, stock.year)
//        }
//    }
//}