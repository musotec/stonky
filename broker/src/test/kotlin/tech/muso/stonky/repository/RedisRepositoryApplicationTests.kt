package tech.muso.stonky.repository

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RedisRepositoryApplicationTests {

	@Test
	fun contextLoads() {
	}

	@Test
	fun testDataCaching() {	// TODO
		// api.getPriceHistory(AAPL)
		// -> cache miss
		// ->> query db, db miss
		// --> do api call
		// <-- return prices
		// api.getPriceHistory(AAPL)
		// -> cache hit
		// <- return prices
		// flood cache to remove AAPL
		// api.getPriceHistory(AAPL)
		// -> cache miss
		// ->> query db, db hit
		// <- return prices
	}

}
