package tech.muso.stonky.repository

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisRepositoryApplication

fun main(args: Array<String>) {
	runApplication<RedisRepositoryApplication>(*args)
}
