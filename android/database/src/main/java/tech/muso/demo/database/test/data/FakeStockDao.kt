package tech.muso.demo.database.test.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tech.muso.demo.common.entity.StockEntity
import tech.muso.demo.database.api.StockDao

/**
 * Class for testing Dao operations within the :repos module, without a room database (in :database)
 *
 * Note: here we don't need to have the Fake implementation private, as we will want to test the UI
 *  against this.
 */
class FakeStockDao : StockDao {

    private val stocksList = mutableListOf<StockEntity>()
    private val stocks = MutableLiveData<List<StockEntity>>()

    init {
        stocks.value = stocksList
    }

    fun addStock(stockEntity: StockEntity) {
        stocksList.add(stockEntity)
        stocks.value = stocksList
    }

    override fun getStocks(): LiveData<List<StockEntity>> = stocks

    override fun getStocksByName(search: String): LiveData<List<StockEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertAll(stockEntities: List<StockEntity>) {
        this.stocks.value = stockEntities
    }

}