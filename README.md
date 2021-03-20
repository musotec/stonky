# stonky
stonky is a [ktor microservice](https://ktor.io/) that performs simulation (and in the future, playback) of price history data,
in the simulating the behavior of real stock market APIs.

Currently, the data is only in the format of the [Alpaca API (v2)](https://alpaca.markets/docs/api-documentation/api-v2/).

## Getting Started
stonky can be downloaded from the [releases](https://github.com/musotec/stonky/releases/) section as a fat jar.
After downloaded, the tomcat server can be launched using `java -jar /path/to/downloaded/release.jar`.

By default, the server runs on port `8080`, but this can be changed using command line option `-port` (unverified).

## Interfacing with the server
Currently, the server can be interfaced with by navigating to [http://localhost:8080](http://127.0.0.1:8080/). 
The root directory displays a guide (or it will), describing the use and verification with the example kotlin client.


## WebSocket API
The WebSocket API is planned, but not implemented fully.

For now, you can simulate candles only, which is achieved by any WebSocket.Frame sent to

[http://127.0.0.1:8080/test/v1/bars](http://127.0.0.1:8080/test/v1/bars)

### Building
The jar for the project can be updated from source by running the gradle ShadowJar gradle task (use `gradlew.bat` on Windows).
```
$ gradlew :shadowJar
```

The jar is then output in `/build/libs/` as `ktor-stonky-X.X.X-SNAPSHOT-all.jar`

It can then be run with `java -jar ktor-stonky-X.X.X-SNAPSHOT-all.jar`.