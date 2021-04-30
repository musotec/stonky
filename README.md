# stonky
**stonky** is a full-stack multiplatform finance application written entirely in kotlin.

**stonky.server** - is a [ktor microservice](https://ktor.io/) that acts as a middle man for stock market API data,
allowing for playback control and interfacing with price history data streams.

**stonky.repository** - is a [spring microservice](https://spring.io/projects/spring-boot) responsible as the broker
between remote stock market APIs and *stonky.server*.
Allowing for smart rate limiting and retrieval from a local Redis cache when applicable.

**stonky.android** - a native android application for interfacing with the locally running *stonky.server*.

Currently, the emulated data playback is output in the format of the [Alpaca API (v2)](https://alpaca.markets/docs/api-documentation/api-v2/).

## Development Timeline
- ✅~~Android Client Interface~~
- ✅~~Client Mock Portfolio UI~~
- ✅~~Initial Multiplatform Project Integration~~
- ⭕ Local Redis server with API caching
- Alpaca v2 API as incoming data source
- Web Frontend Client (based on [lightweight-charts](https://github.com/tradingview/lightweight-charts))
- REST Server Portfolio endpoint
- Android Client Paper Trading and Portfolio
- Web Client Portfolio
- Multiplatform Project Optimization for CI

## Getting Started
stonky can be downloaded from the [releases](https://github.com/musotec/stonky/releases/) section as a fat jar and config file.

In the directory of the downloaded jar, create the `config.yaml` and set any API keys.
The local server can then be launched using `java -jar /path/to/downloaded/release.jar`.

If you wish to build the project on your own. Read the [setup documentation](SETUP.md).

## WebSocket API
The WebSocket API (using the default config) is hosted at `ws://127.0.0.1:8080/api`.

- Strings are interpreted as a ticker symbol, and are submitted to the TD Ameritrade [PriceHistory](https://developer.tdameritrade.com/price-history/apis/get/marketdata/%7Bsymbol%7D/pricehistory) API.
- Mock data has not been tested, but is available using the symbol `MOCK`

Subsequent calls to the WebSocket will increment the tape, giving the next candle until there are no more candles to increment through.
At any time during this, the String `CLOSE` will terminate the session, allowing for a new stock symbol to be entered.

## (WIP) Server Frontend
Currently, the server can be interfaced with by navigating to [http://localhost:8080](http://127.0.0.1:8080/).
The root directory displays an (in progress) interface that contains a simple reactive list with Kotlin/JS as a proof of concept.
