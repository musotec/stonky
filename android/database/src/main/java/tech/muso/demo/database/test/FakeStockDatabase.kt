package tech.muso.demo.database.test

import tech.muso.demo.database.test.data.FakeStockDao

class FakeStockDatabase private constructor() {

    var stockDao = FakeStockDao()
        private set

    // java style singleton object for this class
    companion object {
        @Volatile
        private var instance: FakeStockDatabase? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FakeStockDatabase().also { instance = it }
            }
    }
}