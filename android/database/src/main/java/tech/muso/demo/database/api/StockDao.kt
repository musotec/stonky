package tech.muso.demo.database.api

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tech.muso.demo.common.entity.StockEntity

/**
 * The DAO (Data Access Object) for the Stock class.
 * This defines what methods we want to expose as endpoints for interfacing with the database.
 *
 * We are also adding Room annotations in order for it to generate it's database implementation.
 */
@Dao
interface StockDao {
    @Query("SELECT * FROM StockEntity ORDER BY id")
    /** @return LiveData that holds a new list of [StockEntity] objects any time the table is updated. */
    fun getStocks(): LiveData<List<StockEntity>>

    @Query("SELECT * FROM StockEntity WHERE id LIKE :search ORDER BY name")
    fun getStocksByName(search: String): LiveData<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stockEntities: List<StockEntity>)
}
