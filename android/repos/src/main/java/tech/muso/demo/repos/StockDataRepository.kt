package tech.muso.demo.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.*
import tech.muso.demo.common.entity.StockEntity
import tech.muso.demo.database.api.StockDao
import tech.muso.demo.repos.api.StockRepo
import tech.muso.demo.service.StockService
import tech.muso.demo.service.StockWatchlistWebService

/**
 * Stock Data Repository for handling operations for interfacing with the Stock Data.
 */
class StockDataRepository private constructor(
    private val stockDao: StockDao,
    private val stockService: StockService,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): StockRepo {

    // java style singleton object for this class
    companion object {
        @Volatile
        private var instance: StockDataRepository? = null

        fun getInstance(stockDao: StockDao, stockService: StockService = StockWatchlistWebService()) =
            instance ?: synchronized(this) {
                instance ?: StockDataRepository(stockDao, stockService).also { instance = it }
            }
    }

    override val stocks: LiveData<List<StockEntity>> = liveData<List<StockEntity>> {
        // get LiveData object from Room Database DAO
        val stocksLiveData = stockDao.getStocks()
        emitSource(stocksLiveData)
    }

    override fun filterStocksBySubstring(search: String): LiveData<List<StockEntity>> {
        return stockDao.getStocks()
    }

    /**
     * Return true if our repository cached data is out of date.
     */
    private suspend fun shouldUpdateCache(): Boolean {
        return true
    }

    /**
     * Update our cached list of stocks if necessary.
     */
    suspend fun tryUpdateStockCache() {
        if (shouldUpdateCache()) fetchRecentStocksList()
    }

    /**
     * Update the repository data by getting new data from our [StockService].
     */
    suspend fun fetchRecentStocksList() {
        val stocks = stockService.getAllStocks()
        stockDao.insertAll(stocks)
    }
}