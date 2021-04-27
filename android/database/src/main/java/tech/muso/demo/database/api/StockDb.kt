package tech.muso.demo.database.api

import android.content.Context
import tech.muso.demo.database.AppDatabase

/**
 * This class is needed so that the AppDatabase class does not leak the RoomDatabase dependency to
 * modules that wish to obtain the Singleton.
 *
 * With this abstraction. We can simply wrap the Database, and provide the DAO via this singleton.
 * It operates exactly the same as the AppDatabase one.
 *
 * This at first appears excessive, but is necessary due to how Room generates the source files.
 */
class StockDb private constructor(roomDatabase: AppDatabase) {

    // expose the StockDao without exposing the RoomDatabase creation
    val stockDao: StockDao = roomDatabase.stockDao()

    // java style singleton object for this class
    companion object {
        @Volatile private var instance: StockDb? = null

        // NOTE: this context must be an application context to perform the database instantiation
        fun getInstance(context: Context): StockDb {
            return instance ?: synchronized(this) {
                instance ?: init(context).also { instance = it }
            }
        }

        // inject the RoomDatabase into the private constructor so that we can expose only the DAO
        private fun init(context: Context): StockDb {
            return StockDb(AppDatabase.getInstance(context))
        }
    }
}