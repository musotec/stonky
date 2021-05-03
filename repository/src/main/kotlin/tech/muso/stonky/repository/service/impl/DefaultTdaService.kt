package tech.muso.stonky.repository.service.impl

import Bars
import Candle
import com.studerw.tda.client.HttpTdaClient
import com.studerw.tda.model.history.FrequencyType
import com.studerw.tda.model.history.PriceHistReq
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import tech.muso.stonky.config.Config
import tech.muso.stonky.repository.service.TdaService
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class DefaultTdaService(
    val template: RedisTemplate<String, Candle>
) : TdaService {

    class TdaClient private constructor() {

        // java style singleton object for this class
        companion object {
            @Volatile
            private var instance: TdaClient? = null

            fun getInstance() : HttpTdaClient =
                instance?.client ?: synchronized(this) {
                    instance?.client ?: TdaClient().also { instance = it }.run { client }
                }
        }

        private val tdaConf = Config.ApiKeys.TDAmeritrade ?: throw RuntimeException(
            """Setup your config.yaml with the following:
            |
            |api-keys:
            |  tda:
            |    refresh-token: "YOUR REFRESH TOKEN"
            |    consumer-key: "YOUR CONSUMER KEY"
            |""".trimMargin()
        )

        private val props = Properties().apply {
            setProperty("tda.client_id", tdaConf.`consumer-key`)
            setProperty("tda.token.refresh", tdaConf.`refresh-token`)
        }

        private val client = HttpTdaClient(props)
    }

    /**
     * Convert from TD Ameritrade (v2) PriceHistory Candle to our Candle
     */
    private fun com.studerw.tda.model.history.Candle.toCandle(): Candle {
        return Candle(
            timeSeconds = (datetime / 1000).toInt(),
            close = close.toDouble(),
            high = high.toDouble(),
            low = low.toDouble(),
            open = open.toDouble(),
            volume = volume.toInt()
        )
    }



    override fun getCachedCandles(symbol: String, table: String, offset: Long, startTimeEpochSeconds: Long): Bars {
        val dayName = LocalDateTime.ofEpochSecond(offset, 0, ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE)

        // TODO: determine how many different days to span (currently just one) and return the joined sets across the tables.
        //  NOTE: offset will need to change with the day + 24*60*60
        val l: Set<Candle> = template.opsForZSet().rangeByScore("$table:$dayName", (startTimeEpochSeconds - offset).toDouble(), Double.MAX_VALUE) as Set<Candle>

        // Alternative, which returns all the data for the day.
//        val l = template.opsForZSet().range("$table:$dayName", 0, -1) as Set<Candle>
        return Bars(symbol, l.toList())
    }

    override fun cacheCandles(table: String, candles: List<Candle>, offset: Long): Boolean? {
        println("CACHING CANDLES IN TABLE $table")
        val set: Set<ZSetOperations.TypedTuple<Candle>> =
            candles.map { DefaultTypedTuple<Candle>(it, (it.timeSeconds - offset).toDouble()) }.toSet()
        template.opsForZSet().add(table, set)
        return true
    }

    override fun getCandles(symbol: String, unixTimestampSeconds: Long?): Bars {
        var adjustedTimestamp: Long? = null

        // if symbol is already in the repository return the cached version.
        if (unixTimestampSeconds != null) {
//            println("GOT TIMESTAMP $startTimeEpochSeconds")
            val dateTime = LocalDateTime.ofEpochSecond(unixTimestampSeconds, 0, ZoneOffset.UTC)
                .withHour(12)
                .withMinute(30)
                .withSecond(0)
            adjustedTimestamp = dateTime.toEpochSecond(ZoneOffset.UTC)

            val startLocalDate = dateTime.withHour(4).withMinute(0).withSecond(0)
            val offset = startLocalDate.toEpochSecond(ZoneOffset.UTC)
            val tableName = "candles:$symbol"

            println("LOOKING IN TABLE $tableName")

            val bars = getCachedCandles(symbol, tableName, offset, adjustedTimestamp)

            println("RESULT: $bars")
            if (bars.candles.isNotEmpty()) return bars
        }

        // otherwise do a query, cache it, and then return.
        val tdaClient = TdaClient.getInstance()

        val request = PriceHistReq.Builder.priceHistReq()
            .withSymbol(symbol)
            .withStartDate(adjustedTimestamp)
            .withPeriod(1)  // 1 day of candles (note: could be 10/call)
            .withFrequencyType(FrequencyType.minute)
            .withFrequency(1)
            .build()
        val priceHistory = tdaClient.priceHistory(request)

        // TODO: Refactor this code to an object/method call when we have structure
        val startLocalDate = LocalDateTime.ofEpochSecond(priceHistory.candles.first().datetime / 1000, 0, ZoneOffset.UTC)
            .withHour(4).withMinute(0).withSecond(0)
        val startOffset = startLocalDate.toEpochSecond(ZoneOffset.UTC)
        val tableName = "candles:$symbol"


        // TODO: Should cache first and then return from the cache?
        return Bars(symbol, priceHistory.candles.map { it.toCandle() }).also {

            val days = mutableSetOf<List<Candle>>()
            var curr = mutableListOf<Candle>()
            var d = 1
            it.candles.forEach { c ->
                // jump to a new day if enough time has passed
                if (c.timeSeconds > (startOffset + d * 24*60*60)) {
                    days.add(curr)
                    curr = mutableListOf()
                    d++
                }

                // add the candle
                curr.add(c)
            }

            // cache each day to its own sorted set
            days.forEach {
                val date = LocalDateTime.ofEpochSecond(it.first().timeSeconds.toLong(), 0, ZoneOffset.UTC)
                    .withHour(4).withMinute(0).withSecond(0)
                val basicDate = date.format(DateTimeFormatter.BASIC_ISO_DATE)
                val offset = date.toEpochSecond(ZoneOffset.UTC)
                cacheCandles("$tableName:$basicDate", it, offset)
            }
        }
    }
}