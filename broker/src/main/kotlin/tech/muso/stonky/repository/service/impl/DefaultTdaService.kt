package tech.muso.stonky.repository.service.impl

import Bars
import Candle
import com.studerw.tda.client.HttpTdaClient
import com.studerw.tda.model.history.FrequencyType
import com.studerw.tda.model.history.PriceHistReq
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    companion object {
        const val TABLE_NAME = "c"
    }

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



    override fun getCachedCandles(symbol: String, offset: Long, startTimeEpochSeconds: Long): Bars {
        val day = offset.toBasicIsoDate()
        val tableName = "$symbol:$day:$TABLE_NAME"
        println("LOOKING IN TABLE $tableName")

        // TODO: determine how many different days to span (currently just one) and return the joined sets across the tables.
        //  NOTE: offset will need to change with the day + 24*60*60
        val l: Set<Candle> = template.opsForZSet().rangeByScore(tableName, (startTimeEpochSeconds - offset).toDouble(), Double.MAX_VALUE) as Set<Candle>

        if (l.isNotEmpty()) {
            println("CACHE HIT: $tableName")
        }

        // Alternative, which returns all the data for the day.
//        val l = template.opsForZSet().range("$table:$dayName", 0, -1) as Set<Candle>
        return Bars(symbol, l.toList())
    }

    override suspend fun cacheCandles(
        symbol: String,
        date: String,
        table: String,
        candles: List<Candle>,
        offset: Long
    ): Boolean? {
        val tableName = "$symbol:$date:$table"

        println("CACHING CANDLES IN TABLE $tableName")
        val set: Set<ZSetOperations.TypedTuple<Candle>> =
            candles.map { DefaultTypedTuple<Candle>(it, (it.timeSeconds - offset).toDouble()) }.toSet()
        template.opsForZSet().add(tableName, set)
        return true
    }

    override fun getCandles(symbol: String, epochTimeSeconds: Long): Bars {
        val day = DayOfEpoch(epochTimeSeconds)
        // if symbol is already in the repository return the cached version.

        val bars = getCachedCandles(symbol, day.offsetDayEpochSeconds, day.marketStartTimestamp)
        if (bars.candles.isNotEmpty()) {
            return bars
        }

        // otherwise do a query, cache it, and then return.
        val tdaClient = TdaClient.getInstance()

        val request = PriceHistReq.Builder.priceHistReq()
            .withSymbol(symbol)
            .withStartDate(day.offsetDayEpochSeconds)
            .withPeriod(1)  // 1 day of candles (note: could be 10/call)
            .withFrequencyType(FrequencyType.minute)
            .withFrequency(1)
            .build()
        val priceHistory = tdaClient.priceHistory(request)

        // TODO: Should cache first and then return from the cache?
        return Bars(symbol, priceHistory.candles.map { it.toCandle() }).also { cacheBars(day.offsetDayEpochSeconds, it) }
    }

    private fun cacheBars(offset: Long, bars: Bars) {
        val table = "${TABLE_NAME}:${bars.symbol}"
        val days = mutableSetOf<List<Candle>>()
        var curr = mutableListOf<Candle>()
        var d = 1
        bars.candles.forEach { c ->
            // jump to a new day if enough time has passed
            if (c.timeSeconds > (offset + d * 24*60*60)) {
                days.add(curr)
                curr = mutableListOf()
                d++
            }

            // add the candle
            curr.add(c)
        } // add the last day
        days.add(curr)

        // cache each day to its own sorted set
        days.forEach {
            // TODO: speed up the timestamp -> basicDate conversion
            val date = LocalDateTime.ofEpochSecond(it.first().timeSeconds.toLong(), 0, ZoneOffset.UTC)
                .withHour(4).withMinute(0).withSecond(0)
            val basicDate = date.format(DateTimeFormatter.BASIC_ISO_DATE)
            val offset = date.toEpochSecond(ZoneOffset.UTC)
//            cacheCandles("$table:$basicDate", it, offset)
            GlobalScope.launch { cacheCandles(bars.symbol, basicDate, table, it, offset) }
        }
    }
}