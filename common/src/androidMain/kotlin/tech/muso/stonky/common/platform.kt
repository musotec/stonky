package tech.muso.stonky.common

import android.os.Build
import tech.muso.stonky.config.Config

actual fun getPlatformName(): String {
    return "Android"
}

actual fun getStonkyServerAddress(): String {
    return when {
        isEmulator() -> "10.0.2.2"   // override localhost to 10.0.2.2 (to escape android emulator)
        else -> "127.0.0.1"
    }
}

actual fun getStonkyServerPort(): Int {
    return Config.server.port
}


private fun isEmulator(): Boolean {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator"))
}