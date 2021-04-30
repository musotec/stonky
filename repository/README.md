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

### Debugging
#### Error 500
If you are getting 500 for the response from an API call, this is due to a Redis connection failure.
Make sure your `application.properties` has the correct parameters and that the Redis server is running.

You can launch the redis server from the `:repository:docker:composeUp` gradle task.