package tech.muso.demo.database.api

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tech.muso.demo.common.entity.PortfolioEntity

/**
 * The DAO (Data Access Object) for the Stock class.
 * This defines what methods we want to expose as endpoints for interfacing with the database.
 *
 * We are also adding Room annotations in order for it to generate it's database implementation.
 */
@Dao
interface PortfolioDao {
    @Query("SELECT * FROM PortfolioEntity ORDER BY id")
    /** @return LiveData that holds a new list of [PortfolioEntity] objects any time the table is updated. */
    fun getPortfolio(): LiveData<List<PortfolioEntity>>

    @Query("SELECT * FROM PortfolioEntity WHERE id LIKE :search ORDER BY name")
    fun getPortfolioItemByName(search: String): LiveData<List<PortfolioEntity>>

//    @Query("SELECT * FROM portfolioentity WHERE ORDER BY name")
//    fun getPortfolioItemByName(search: String): LiveData<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)    // update previous by id.
    suspend fun insertAll(stocks: List<PortfolioEntity>)
}
