package tech.muso.stonky.repository.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

@RedisHash("h")
data class HashedStockMetadata(
    @Indexed val symbol: String, // symbol
    val day: String,
    val timestamps: MutableList<Long> = mutableListOf()
) {
//    @Id var id = "$table:$day"

    @get:Id
    var id: String? = null
}