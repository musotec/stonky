import com.studerw.tda.model.history.FrequencyType
import com.studerw.tda.model.history.PriceHistReq
import junit.framework.TestCase
import org.junit.Test
import tech.muso.stonky.repository.TdaRepository

class TDApiTest : TestCase() {

    @Test
    fun testApi() {
        val config = Config.defaultConfig()

        val api = TdaRepository.getInstance(config)
        // TODO: extract above into before

//        val result = api.fetchQuote("/NQ")
//        println(result)
//
//        val result2 = api.fetchQuote("SPY")
//        println(result2)

//        val startDate = 1617661800001L
//        val startDate = 1617059539433L
//                        1617578069751
//        val startDate = System.currentTimeMillis() - 604_800_000L

        // 60 * 1000 = 1 minute
        val startDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 1) // get previous day of price history (at most 3 day weekend)
        val request = PriceHistReq.Builder.priceHistReq()
            .withSymbol("SPY")
            .withStartDate(startDate)
            .withFrequencyType(FrequencyType.minute)
            .withFrequency(1)
            .build()

        println("START DATE: $startDate")
        val result3 = api.priceHistory(request)
        println(result3)
    }

    @Test
    fun testAccountApi() {
        val config = Config.defaultConfig()
        val api = TdaRepository.getInstance(config)
    }

}