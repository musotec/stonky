package tech.muso.stonky.repository.service.impl

import Trade
import TradeSet
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

    override fun getCachedTrades(symbol: String, table: String, offset: Long, startTimeEpochSeconds: Long): TradeSet {
        val dayName = LocalDateTime.ofEpochSecond(offset, 0, ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE)

        val tableName = "$table:$dayName"
        println("LOOKING IN TABLE $tableName")
        // TODO: determine how many different days to span (currently just one) and return the joined sets across the tables.
        //  NOTE: offset will need to change with the day + 24*60*60
//        val l: Set<Trade> = template.opsForZSet().rangeByScore(tableName, (startTimeEpochSeconds - offset).toDouble(), Double.MAX_VALUE) as Set<Trade>

        // Alternative, which returns all the data for the day.
        val l = template.opsForZSet().range(tableName, 0, -1) as Set<Trade>
        return TradeSet(symbol, l.toList(), cached=true)
    }

    override fun cacheTrades(table: String, trades: List<Trade>, offset: Long): Boolean? {
        println("CACHING TRADES IN TABLE $table")
        val set: Set<ZSetOperations.TypedTuple<Trade>> =
            trades.map { DefaultTypedTuple<Trade>(it, ((it.timestamp - offset) * 1_000_000_000 + it.nano).toDouble()) }.toSet()
        template.opsForZSet().add(table, set)
        return true
    }

    private data class TradeDayContaining(val epochTimeSeconds: Long) {
        private val tradeDayStart = LocalDateTime.ofEpochSecond(epochTimeSeconds, 0, ZoneOffset.UTC)
            .withHour(12)
            .withMinute(30)
            .withSecond(0)

        // TODO: does not take into account the underlying and whether or not it uses standard market hours.
        val adjustedTimestamp: Long = tradeDayStart.toEpochSecond(ZoneOffset.UTC)

        // FIXME
        val startLocalDate = tradeDayStart.withHour(4).withMinute(0).withSecond(0)
        val endLocalDate = tradeDayStart.withHour(23).withMinute(0).withSecond(0)

        val offset = startLocalDate.toEpochSecond(ZoneOffset.UTC)
    }

    private inline fun LocalDateTime.toZonedDate() = ZonedDateTime.of(this, ZoneOffset.UTC)

    override fun getTrades(symbol: String, epochTimeSeconds: Long): TradeSet {
        val table = "$TABLE_NAME:$symbol"
        val tradeDay = TradeDayContaining(epochTimeSeconds)

        // load cached trades if we have them
        val tradeSet = getCachedTrades(symbol, table, tradeDay.offset, tradeDay.adjustedTimestamp)

        // return if we had data.
        if (tradeSet.trades.isNotEmpty()) {
            println("CACHE HIT!")
            return tradeSet
        }

        println("CACHE MISS! $table")

        // otherwise do a query, cache it, and then return.
        val client = AlpacaClient.getInstance()

        val response = client.getTrades(symbol,
            tradeDay.startLocalDate.toZonedDate(),
            tradeDay.endLocalDate.toZonedDate(),
            TRADE_FETCH_LIMIT,
            null
        )

        val next = response.nextPageToken
        println("had next token: $next")

        // TODO: Should cache first and then return from the cache?
        return TradeSet(symbol, response.trades.map { it.toTrade() }).also { cacheTradeSet(tradeDay.offset, it) }
    }

    /**
     * Perform an immediate call on the Alpaca API using the next page token to retrieve more results.
     */
//    override fun getMoreTrades(symbol: String, epochTimeSeconds: Long, nextPageToken: String): TradeSet {
//
//        val client = AlpacaClient.getInstance()
//
//        val response = client.getTrades(symbol,
//            ZonedDateTime.of(startLocalDate, ZoneOffset.UTC),
//            ZonedDateTime.of(endLocalDate, ZoneOffset.UTC),
//            TRADE_FETCH_LIMIT,
//            nextPageToken
//        )
//
//    }

    private fun cacheTradeSet(offset: Long, tradeSet: TradeSet) {
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
            val date = LocalDateTime.ofEpochSecond(it.first().timestamp.toLong(), 0, ZoneOffset.UTC)
                .withHour(4).withMinute(0).withSecond(0)
            val basicDate = date.format(DateTimeFormatter.BASIC_ISO_DATE)
            val offset = date.toEpochSecond(ZoneOffset.UTC)
            cacheTrades("$table:$basicDate", it, offset)
        }
    }
}