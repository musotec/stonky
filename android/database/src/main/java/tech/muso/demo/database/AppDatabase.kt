package tech.muso.demo.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.muso.demo.common.entity.PortfolioEntity
import tech.muso.demo.common.entity.StockEntity
import tech.muso.demo.database.api.PortfolioDao
import tech.muso.demo.database.api.StockDao

/**
 * The Room database for this app.
 *
 * This defines all the tables we need in it. Currently, that's just a table of Stock objects.
 */
@Database(entities = [StockEntity::class, PortfolioEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(), StonkyLocalCacheDbInterface {
    abstract override fun stockDao(): StockDao
    abstract override fun portfolioDao(): PortfolioDao

    // java style singleton object for this class, with dependency injection
    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}

/**
 * Define what DAOs are required to be available by the Stock database
 */
interface StonkyLocalCacheDbInterface {
    fun stockDao(): StockDao
    fun portfolioDao(): PortfolioDao
}

/**
 * TODO: when I write the UI connection maybe use this to manually add some test data.
 *   With retrofit service from :repos, we will populate the data anyways.
 */
@VisibleForTesting
private class TestCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        GlobalScope.launch {
//            populateDatabase()
        }
    }
}

private const val DATABASE_NAME = "stocks-db"
