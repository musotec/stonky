package tech.muso.stonky.repository.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Reference
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

@RedisHash("stocks")
data class Stock(
    @Indexed val symbol: String,
    val name: String,
    val year: Int
) {
    @get:Id
    var id: String? = null
    @Indexed @get:Reference var trades: List<TestTrade>? = listOf()
}