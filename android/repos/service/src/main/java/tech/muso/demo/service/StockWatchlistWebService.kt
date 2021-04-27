package tech.muso.demo.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import tech.muso.demo.common.entity.StockEntity

/**
 * This logic contains all the code for the Repository to interface with the Network API.
 *
 * All the network logic should be contained in this module to avoid introducing errors.
 * e.g. Networking on UI, breaking separation of concerns, etc.
 *
 * The API is a Service because we do not want to any more instances of it than we need.
 * For this reason, the StockDataRepository that uses this, implements it as a Singleton.
 */
class StockWatchlistWebService : StockService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gist.githubusercontent.com/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // instantiate retrofit
    private val stockWebService = retrofit.create(StockService::class.java)

    override suspend fun getAllStocks(): List<StockEntity> = withContext(Dispatchers.Default) {
        val result = stockWebService.getAllStocks()
        result.shuffled()
    }

    override suspend fun getUserStocks(): List<StockEntity> = withContext(Dispatchers.Default) {
        val result = stockWebService.getUserStocks()
        result  // could sort by holdings, date, etc.
    }

}

/**
 * Hacked together endpoint that returns a static JSON object by fetching it from our "API".
 *
 * This interface is required for retrofit.
 * It also allows us to use the interface for a fake repository for testing.
 */
abstract interface StockService {
    @GET("musotec/89da0ef3658cb90075893307b046a48a/raw/b14624c9062f26c1de4ac7d82da5ca572e050406/stocks.json")
    suspend fun getAllStocks(): List<StockEntity>

    // TODO: unused, but this would be another endpoint
    suspend fun getUserStocks(): List<StockEntity>
}