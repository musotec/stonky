package tech.muso.stonky.repository.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDate

@RedisHash("trades")
data class TestTrade(
    @Indexed val strFoo: String,
    val strBar: String,
    val date: LocalDate
) {
    @get:Id
    var id: String? = null
}