package tech.muso.stonky.repository

import Candle
import com.studerw.tda.client.HttpTdaClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.lang.RuntimeException
import java.util.*
import tech.muso.stonky.config.Config

class TdaRepository private constructor(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    // java style singleton object for this class
    companion object {
        @Volatile
        private var instance: TdaRepository? = null

        fun getInstance() : HttpTdaClient =
            instance?.client ?: synchronized(this) {
                instance?.client ?: TdaRepository().also { instance = it }.run { client }
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
fun com.studerw.tda.model.history.Candle.toCandle(): Candle {
    return Candle(
        timeSeconds = (datetime / 1000).toInt(),
        close = close.toDouble(),
        high = high.toDouble(),
        low = low.toDouble(),
        open = open.toDouble(),
        volume = volume.toInt()
    )
}
