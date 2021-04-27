package tech.muso.demo.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.*
import tech.muso.demo.common.entity.PortfolioEntity
import tech.muso.demo.database.api.PortfolioDao
import tech.muso.demo.repos.api.CachedPortfolioRepo
import tech.muso.demo.service.PortfolioService
import tech.muso.demo.service.PortfolioWebService

/**
 * Cached Portfolio Data Repository for handling for interfacing with the User Portfolio Data.
 * Pre
 */
class PortfolioDataRepository private constructor(
    private val portfolioDao: PortfolioDao,
    private val portfolioService: PortfolioService,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): CachedPortfolioRepo {

    // java style singleton object for this class
    companion object {
        @Volatile
        private var instance: PortfolioDataRepository? = null

        fun getInstance(portfolioDao: PortfolioDao, stockService: PortfolioService = PortfolioWebService()) =
            instance ?: synchronized(this) {
                instance ?: PortfolioDataRepository(portfolioDao, stockService).also { instance = it }
            }
    }

    override val portfolio: LiveData<List<PortfolioEntity>> = liveData<List<PortfolioEntity>> {
        // get LiveData object from Room Database DAO
        val portfolioLiveData = portfolioDao.getPortfolio()
        emitSource(portfolioLiveData)
    }

    override fun filterBySubstring(search: String): LiveData<List<PortfolioEntity>> {
        return portfolioDao.getPortfolio()  // TODO:
    }

    /**
     * Return true if our repository cached data is out of date.
     */
    private suspend fun shouldUpdateCache(): Boolean {
        return true // TODO: get the current version from the server to check against.
    }

    /**
     * Update our cached list of stocks if necessary.
     */
    suspend fun tryUpdatePortfolioCache() {
        if (shouldUpdateCache()) fetchCurrentPortfolio()
    }

    /**
     * Update the repository data by getting new data from our [PortfolioService].
     */
    suspend fun fetchCurrentPortfolio() {
        val stocks = portfolioService.getRootPortfolio()
        portfolioDao.insertAll(stocks)
    }
}