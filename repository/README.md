# stonky.repository
This module is the repository microservice for stonky. The goal of the module is to provide routing for stonky's own API.
This allows for smart management of API calls, returning locally cached results if available by using a Redis database.

## TODO
Currently, this module is at the project integration level. All Redis components are in their default state.

Roadmap:
- Implement a smarter Redis configuration for saving time-series data.
- Migrate remote API interfacing from `stonky.server` into `stonky.repository`
- Integrate the `config` module for easier configuration outside of this module.

## Rationale
- Spring Boot is used over Ktor as Spring Boot allows for future scale when increasing the number of Redis server nodes. Despite this not being the case here.
- Spring WebFlux is used instead of Spring Async/Spring Web because of the superior non-blocking I/O of WebFlux.
- Lettuce is used as the Redis client due to it's superior asynchronous support over other JVM clients.

## Future Scaling
- When using Redis clusters instead of a single node, work needs to be done to route various tables (can separate by symbol, data type)
    - Ideally, only call SCAN on the node that can use the data instead of naive implementation of SCAN on all nodes in cluster.

### Debugging

#### Connecting to Redis
*Note: this is not the only way to achieve this, but is very straightforward, and uses netcat, included in most linux distros.*

*For Windows you can try [ncat](https://nmap.org/ncat/).*

**Entering redis-cli:**
- Make sure to have **netcat** (*[gnu-netcat](https://archlinux.org/packages/?name=gnu-netcat)*) installed.
- To enter [**redis-cli**](https://redis.io/topics/rediscli#interactive-mode) in *interactive mode* for the Redis node launched via the docker-compose of this repository.
```
$ nc -v 172.17.0.1 6379
localhost [172.17.0.1] 6379 (redis) open
```

**Example using [MONITOR](https://redis.io/commands/monitor):**
```
$ MONITOR
+OK
+1619961076.677202 [0 172.17.0.1:47914] "SADD" "candle:dddd1942-d946-4863-b2cf-e66fb4944e0d:idx" "candle:symbol:QQQ"
+1619961076.677303 [0 172.17.0.1:47914] "SISMEMBER" "candle" "d92f32fd-12b4-48d6-b7b2-c53a33619dad"
+1619961076.677415 [0 172.17.0.1:47914] "DEL" "candle:d92f32fd-12b4-48d6-b7b2-c53a33619dad"
+1619961076.677521 [0 172.17.0.1:47914] "HMSET" "candle:d92f32fd-12b4-48d6-b7b2-c53a33619dad" "_class" "tech.muso.stonky.repository.model.BarCandle" "candle.close" "338.1" "candle.high" "338.1" "candle.low" "338.08" "candle.open" "338.08" "candle.timeSeconds" "1619827140" "candle.volume" "3465" "id" "d92f32fd-12b4-48d6-b7b2-c53a33619dad" "symbol" "QQQ" "time" "1619827140"
+1619961076.677632 [0 172.17.0.1:47914] "SADD" "candle" "d92f32fd-12b4-48d6-b7b2-c53a33619dad"
+1619961076.677731 [0 172.17.0.1:47914] "SADD" "candle:symbol:QQQ" "d92f32fd-12b4-48d6-b7b2-c53a33619dad"
```

See Also: *[Redis (server) Command Reference](https://redis.io/commands#server)*


#### Error 500
If you are getting 500 for the response from an API call, this is due to a Redis connection failure.
Make sure your `application.properties` has the correct parameters and that the Redis server is running.

You can launch the redis server from the `:repository:docker:composeUp` gradle task.