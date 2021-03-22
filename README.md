# stonky
stonky is a [ktor microservice](https://ktor.io/) that performs simulation (and in the future, playback) of price history data,
in the simulating the behavior of real stock market APIs.

Currently, the data is only in the format of the [Alpaca API (v2)](https://alpaca.markets/docs/api-documentation/api-v2/).

## Getting Started
stonky can be downloaded from the [releases](https://github.com/musotec/stonky/releases/) section as a fat jar and config file.

In the directory of the downloaded jar, create the `config.yaml` and set any API keys.
The local server can then be launched using `java -jar /path/to/downloaded/release.jar`.

## Interfacing with the server
Currently, the server can be interfaced with by navigating to [http://localhost:8080](http://127.0.0.1:8080/). 
The root directory displays a guide (or it will), describing the use and verification with the example kotlin client.
To change the port currently, you must set the environment variable `PORT` for your system. Otherwise, it will run on port 8080 despite changes to `config.yaml`.

## WebSocket API
The WebSocket API is currently in development.
By sending a `Frame.Text(String)` you can select the mode. 

Inputting `MOCK` will mock stock behavior following the Black Scholes model.

Otherwise, any other ticker symbol will be submitted to the TD Ameritrade [PriceHistory](https://developer.tdameritrade.com/price-history/apis/get/marketdata/%7Bsymbol%7D/pricehistory) API. 

Subsequent calls to the WebSocket will increment the tape, giving the next candle until there are no more candles to increment through.

At any time during this, the String `CLOSE` will terminate the session, allowing for a new stock symbol to be entered.

### Building
The jar for the project can be updated from source by running the gradle ShadowJar gradle task (use `gradlew.bat` on Windows).
```
$ gradlew :shadowJar
```

The jar is then output in `/build/libs/` as `ktor-stonky-X.X.X-SNAPSHOT-all.jar`

It can then be run with `java -jar ktor-stonky-X.X.X-SNAPSHOT-all.jar`.