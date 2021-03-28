# stonky
stonky is a [ktor microservice](https://ktor.io/) that performs local simulation and playback of price history data,
to simulate the behavior of real stock market APIs without rate limits.

Currently, the data is only output in the format of the [Alpaca API (v2)](https://alpaca.markets/docs/api-documentation/api-v2/).

## Getting Started
stonky can be downloaded from the [releases](https://github.com/musotec/stonky/releases/) section as a fat jar and config file.

In the directory of the downloaded jar, create the `config.yaml` and set any API keys.
The local server can then be launched using `java -jar /path/to/downloaded/release.jar`.

## WebSocket API
The WebSocket API (using the default config) is hosted at `ws://127.0.0.1:8080/test/v1/bars`.

By sending a `Frame.Text(operationMode: String)` you can select the operation mode. Current modes are *Mock Candles* and *Stock Price History*

**Operation Mode Strings**
- `"MOCK"` - simulates trades and build candles following a Gaussian/Normal distribution, as per the Black Scholes model.
- Any other string is interpreted as a ticker symbol, and will be submitted to the TD Ameritrade [PriceHistory](https://developer.tdameritrade.com/price-history/apis/get/marketdata/%7Bsymbol%7D/pricehistory) API. 

Subsequent calls to the WebSocket will increment the tape, giving the next candle until there are no more candles to increment through.

At any time during this, the String `CLOSE` will terminate the session, allowing for a new stock symbol to be entered.

## (WIP) Viewing Live Data Output from the Server
Currently, the server can be interfaced with by navigating to [http://localhost:8080](http://127.0.0.1:8080/).
The root directory displays an (in progress) interface that shows outgoing candle data for monitoring.

### Building
The jar for the project can be updated from source by running the gradle ShadowJar gradle task (use `gradlew.bat` on Windows).
```
$ gradlew :shadowJar
```

The jar is then output in `/build/libs/` as `ktor-stonky-X.X.X-SNAPSHOT-all.jar`

It can then be run with `java -jar ktor-stonky-X.X.X-SNAPSHOT-all.jar`. Make sure to have a `config.yaml` in the directory you are executing in.