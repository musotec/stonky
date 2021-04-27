package tech.muso.stonky.common

import tech.muso.stonky.config.Config

actual fun getPlatformName(): String {
    return "Server"
}

actual fun getStonkyServerAddress(): String {
    return "127.0.0.1"
}

actual fun getStonkyServerPort(): Int {
    return Config.server.port
}