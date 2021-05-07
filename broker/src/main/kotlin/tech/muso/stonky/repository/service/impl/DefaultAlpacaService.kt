package tech.muso.stonky.repository.service.impl

import Trade
import TradeSet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.jacobpeterson.alpaca.AlpacaAPI
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import tech.muso.stonky.repository.service.AlpacaService
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class DefaultAlpacaService(
    val template: RedisTemplate<String, Trade>
) : AlpacaService {

    companion object {
        const val TABLE_NAME = "trades"
        const val TRADE_FETCH_LIMIT = 10_000

        @JvmStatic val rfc3339 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))

        @JvmStatic val localNYSE =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.of("America/New_York"))

        fun parseEpoch(time: String) = Instant.parse(time).toEpochMilli()
        fun parseToEpochSecond(time: String) = Instant.parse(time).truncatedTo(ChronoUnit.SECONDS).toEpochMilli()
    }

    class AlpacaClient private constructor() {
        private val client = AlpacaAPI(
        )    // TODO: configure this manually for the Service type (debug/release/etc)
        // java style singleton object for this class
        companion object {
            @Volatile
            private var instance: AlpacaClient? = null

            fun getInstance() : AlpacaAPI =
                instance?.client ?: synchronized(this) {
                    instance?.client ?: AlpacaClient().also { instance = it }.run { client }
                }
        }
    }

    /**
     * Convert from Alpaca (v2) Trade to our Trade, which only stores the epoch second resolution.
     */
    private fun net.jacobpeterson.domain.alpaca.marketdata.historical.trade.Trade.toTrade(): Trade {
        return Trade(
            timestamp = t.toEpochSecond(),
            nano = t.nano,
            exchange = x,
            price = p,
            size = s,
            conditions = c,
            id = i,
            tape = z
        )
    }

    override fun getCachedTrades(symbol: String, table: String, offset: Long, startTimeEpochSeconds: Long, endTimeEpochSeconds: Long): TradeSet {
        val day = offset.toBasicIsoDate()
        val tableName = "$table:$day"
        println("LOOKING IN TABLE $tableName")
        // TODO: determine how many different days to span (currently just one) and return the joined sets across the tables.
        //  NOTE: offset will need to change with the day + 24*60*60
        val l: Set<Trade> = template.opsForZSet().rangeByScore(tableName, (startTimeEpochSeconds - offset).toDouble(), (endTimeEpochSeconds - offset).toDouble()) as Set<Trade>

        // Alternative, which returns all the data for the day. (not just in range)
//        val l = template.opsForZSet().range(tableName, 0, -1) as Set<Trade>
        return TradeSet(symbol, l.toList(), cached=true)
    }

    override fun cacheTrades(table: String, trades: List<Trade>, offset: Long): Boolean? {
        println("CACHING TRADES IN TABLE $table")
        val set: Set<ZSetOperations.TypedTuple<Trade>> = // remove offsetEpochDayStart from time; shift + nano for score
            trades.map { DefaultTypedTuple<Trade>(it, ((it.timestamp - offset) * 1_000_000_000 + it.nano).toDouble()) }.toSet()
        template.opsForZSet().add(table, set)
        return true
    }

    override fun getTrades(symbol: String, epochTimeSeconds: Long): TradeSet {
        val table = "$TABLE_NAME:$symbol"
        val day = DayOfEpoch(epochTimeSeconds)

        // load cached trades if we have them
        val tradeSet = getCachedTrades(symbol, table, day.offsetDayEpochSeconds, day.marketStartTimestamp, day.marketEndTimestamp)

        // return if we had data.
        if (tradeSet.trades.isNotEmpty()) {
            println("CACHE HIT!")
            return tradeSet
        }

        println("CACHE MISS! $table")

        // otherwise do a query, cache it, and then return.
        val client = AlpacaClient.getInstance()

        val response = client.getTrades(symbol,
            day.startLocalDate.toZonedDate(),
            day.endLocalDate.toZonedDate(),
            TRADE_FETCH_LIMIT,
            null
        )

        val next = response.nextPageToken
        println("had next token: $next")

        // TODO: Should cache first and then return from the cache?
        return TradeSet(
            symbol,
            response.trades.map { it.toTrade() },
    false,
            next
        ).also {
            GlobalScope.launch { cacheTradeSet(day.offsetDayEpochSeconds, it) }
        }
    }

    override fun getBulkTradesFromDate(symbol: String, epochTimeSeconds: Long) {
        val present = System.currentTimeMillis() / 1000 // convert to epoch seconds
        var tradeDay = DayOfEpoch(epochTimeSeconds)
        // prevent what would be an erroneous call to Alpaca API
        if (tradeDay.endLocalDate.toEpochSecond(ZoneOffset.UTC) >= present) {
            println("<< REACHED CURRENT DAY ")
            return
        }

        GlobalScope.launch {
            mutex.withLock {
                cacheTradesOfDay(symbol, epochTimeSeconds) {
                    tradeDay = DayOfEpoch(tradeDay.marketStartTimestamp + 24 * 60 * 60)
                    println("=============================================================================")
                    println(">> ${tradeDay.epochTimeSeconds.toBasicIsoDate()} ")
                    println("=============================================================================")
                    getBulkTradesFromDate(symbol, tradeDay.epochTimeSeconds)
                }
            }
        }
    }


    /**
     * Perform an immediate call on the Alpaca API using the next page token to retrieve more results.
     */
    private suspend fun cacheTradesOfDay(symbol: String, epochTimeSeconds: Long, nextPageToken: String? = null, doOnEnd: () -> Unit) {
        val client = AlpacaClient.getInstance()
        val tradeDay = DayOfEpoch(epochTimeSeconds)

//        val s = tradeDay.startLocalDate.toZonedDate().format(localNYSE)
//        val e = tradeDay.endLocalDate.toZonedDate().format(localNYSE)
//        println("GETTING [$s, $e]")

        val response = client.getTrades(
            symbol,
            tradeDay.startLocalDate.toZonedDate(),
            tradeDay.endLocalDate.toZonedDate(),
            TRADE_FETCH_LIMIT,
            nextPageToken
        )

        val next = response.nextPageToken

        TradeSet(
            symbol,
            response.trades.map { it.toTrade() },
            false,
            next
        ).also {
            cacheTradeSet(tradeDay.offsetDayEpochSeconds, it)
            if (it.trades.isNotEmpty()) println(" - CACHED UNTIL ${it.trades.last().timestamp} [count=${it.trades.size}]")
            delay(100)  // delay for Alpaca API throttle    // TODO: test if delay from data receive or request send. (cacheTradeSet may take a while)
            if (next != null) {
                println(" -> next: $next")
                cacheTradesOfDay(symbol, epochTimeSeconds, next, doOnEnd)
            } else {
                if (it.trades.isNotEmpty()) println(" <- end of stream ${it.trades.last().timestamp}")
                doOnEnd()
            }
        }
    }


    /**
     * Perform an immediate call on the Alpaca API using the next page token to retrieve more results.
     */
    override fun forceCacheTradesOfDay(symbol: String, epochTimeSeconds: Long, nextPageToken: String?): TradeSet {
        val client = AlpacaClient.getInstance()
        val tradeDay = DayOfEpoch(epochTimeSeconds)

//        val s = tradeDay.startLocalDate.toZonedDate().format(localNYSE)
//        val e = tradeDay.endLocalDate.toZonedDate().format(localNYSE)
//        println("GETTING [$s, $e]")

        val response = client.getTrades(symbol,
            tradeDay.startLocalDate.toZonedDate(),
            tradeDay.endLocalDate.toZonedDate(),
            TRADE_FETCH_LIMIT,
            nextPageToken
        )

        val next = response.nextPageToken

        return TradeSet(
            symbol,
            response.trades.map { it.toTrade() },
            false,
            next
        ).also {
            GlobalScope.launch {
                cacheTradeSet(tradeDay.offsetDayEpochSeconds, it)
                if (it.trades.isNotEmpty()) println(" - CACHED UNTIL ${it.trades.last().timestamp} [count=${it.trades.size}]")
                mutex.withLock {
                    delay(200)  // delay for Alpaca API throttle    // TODO: test if delay from data receive or request send.
                    if (next != null) {
                        println(" -> next: $next")
                        forceCacheTradesOfDay(symbol, epochTimeSeconds, next)
                    } else {
                        if (it.trades.isNotEmpty()) println(" <- end of stream ${it.trades.last().timestamp}")
                    }
                }
            }
        }
    }

    private val mutex = Mutex()

    private suspend fun cacheTradeSet(offset: Long, tradeSet: TradeSet) {
        val table = "$TABLE_NAME:${tradeSet.symbol}"
        val days = mutableSetOf<List<Trade>>()
        var curr = mutableListOf<Trade>()
        var d = 1

        tradeSet.trades.forEach {
//                println("$offset : $it")
            // jump to a new day if enough time has passed
            if (it.timestamp > (offset + d * 24*60*60)) {
                days.add(curr)
                curr = mutableListOf()
                d++
            }
            // add the candle
            curr.add(it)
        } // add the last day
        days.add(curr)

        // cache each day to its own sorted set
        days.forEach {
            if (it.isNotEmpty()) {
                // TODO: speed up the timestamp -> basicDate conversion
                val date = LocalDateTime.ofEpochSecond(it.first().timestamp.toLong(), 0, ZoneOffset.UTC)
                    .withHour(4).withMinute(0).withSecond(0)
                val basicDate = date.format(DateTimeFormatter.BASIC_ISO_DATE)
                val offset = date.toEpochSecond(ZoneOffset.UTC)
                cacheTrades("$table:$basicDate", it, offset)
            }
        }
    }
}