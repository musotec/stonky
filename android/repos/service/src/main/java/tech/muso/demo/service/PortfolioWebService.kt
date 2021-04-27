package tech.muso.demo.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import tech.muso.demo.common.entity.PortfolioEntity
import tech.muso.stonky.common.getStonkyServerAddress
import tech.muso.stonky.common.getStonkyServerPort

/**
 * This logic contains all the code for the Repository to interface with the Network API.
 *
 * All the network logic should be contained in this module to avoid introducing errors.
 * e.g. Networking on UI, breaking separation of concerns, etc.
 *
 * The API is a Service because we do not want to any more instances of it than we need.
 * For this reason, the StockDataRepository that uses this, implements it as a Singleton.
 */
class PortfolioWebService : PortfolioService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://${getStonkyServerAddress()}:${getStonkyServerPort()}/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // instantiate retrofit
    private val stockWebService = retrofit.create(PortfolioService::class.java)

    override suspend fun getRootPortfolio(): List<PortfolioEntity> = withContext(Dispatchers.Default) {
        val result = stockWebService.getRootPortfolio()
        result.shuffled()
    }

    override suspend fun getUserWatchlist(): List<PortfolioEntity> = withContext(Dispatchers.Default) {
        val result = stockWebService.getUserWatchlist()
        result  // could sort by holdings, date, etc.
    }

}

/**
 * Retrofit interface to allow connection to the stonky web repository.
 */
abstract interface PortfolioService {
    @GET("musotec/89da0ef3658cb90075893307b046a48a/raw/b14624c9062f26c1de4ac7d82da5ca572e050406/stocks.json")
    suspend fun getRootPortfolio(): List<PortfolioEntity>

    // TODO: unused, but this would be another endpoint
    suspend fun getUserWatchlist(): List<PortfolioEntity>
}