package tech.muso.stonky.repository

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.enums.api.DataAPIType
import net.jacobpeterson.alpaca.enums.api.EndpointAPIType
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

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

	@Test
	fun testTdaApi() {

	}


	@Test
	fun testAlpacaApi() {
		val symbol = "QQQ"
		val epochTimeSeconds = 1620237428L
		val dateTime = LocalDateTime.ofEpochSecond(epochTimeSeconds, 0, ZoneOffset.UTC)
			.withHour(12)
			.withMinute(30)
			.withSecond(0)
		val startLocalDate = dateTime.withHour(4).withMinute(0).withSecond(0)
		val endLocalDate = dateTime.withHour(23).withMinute(0).withSecond(0)

		val client = AlpacaAPI(
			// i know i committed these keys, it's a paper trading account + invalid anyways, pls do not make an issue.
			"2a168730416f6541bbd1ad469527f83b",
			"2f3c00fa56d4498fbbe10eed55cbb0d2dbf09982",
			"4ad11013-d1f9-4e3d-b88e-394444be0144",
			EndpointAPIType.PAPER,
			DataAPIType.SIP
		)

		val clock = client.getClock()
		println(clock)

		val response = client.getTrades(symbol,
			ZonedDateTime.of(startLocalDate, ZoneOffset.UTC),
			ZonedDateTime.of(endLocalDate, ZoneOffset.UTC),
			1_000,
			null
		)

		print(response.trades)
	}
}
