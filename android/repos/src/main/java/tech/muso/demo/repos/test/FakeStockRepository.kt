package tech.muso.demo.repos.test

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import tech.muso.demo.common.entity.StockEntity
import tech.muso.demo.repos.api.StockRepo
import tech.muso.demo.database.test.data.FakeStockDao

/**
 * This Fake version is used for testing (within this module).
 *
 * It should be created by the developer writing the real version to better support Unit Testing.
 *
 * Using Fakes allows for better testing, as the developer writing the real version knows how
 * the real implementation works. And allows for custom methods to be implemented for tests cases.
 *
 * If another developer writes the tests (which is ideal), then this avoids any issues that can
 * commonly occur when having someone who writes tests try to mock the class.
 */
@VisibleForTesting
internal class FakeStockRepository private constructor(private val stockDao: FakeStockDao) : StockRepo {

    // singleton object
    companion object {
        @Volatile private var instance: FakeStockRepository? = null

        fun getInstance(stockDao: FakeStockDao) =
            instance ?: synchronized(this) {
                instance ?: FakeStockRepository(stockDao).also { instance = it }
            }
    }


    override val stocks: LiveData<List<StockEntity>>
        get() = stockDao.getStocks()

    override fun filterStocksBySubstring(search: String): LiveData<List<StockEntity>> {
        TODO("Not yet implemented")
    }

    /**
     * Example methods that would be useful for writing tests.
     */
    fun addStock(stockEntity: StockEntity) {
        stockDao.addStock(stockEntity)
    }

    /**
     * Set the stock with the symbol in the list of stocks to an Error state.
     * e.g. representing a network connection issue, out of date data, etc.
     */
    fun setStockError(symbol: String) {}
}