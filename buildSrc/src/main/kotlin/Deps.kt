@file:Suppress("ClassName", "SpellCheckingInspection")

object deps {
    const val asm = "org.ow2.asm:asm:7.1"
    const val jsr305 = "com.google.code.findbugs:jsr305:3.0.2"

    object android {
        const val gradlePlugin = "com.android.tools.build:gradle:4.0.1"  // old version needed for IntelliJ 4.1 support
    }

    object kotlin {

        // https://kotlinlang.org/docs/releases.html#release-details (strict version alignment needed for kotlin multiplatform)
        const val version = "1.4.32"
        const val coreVersion = "1.4.0" // version for multiplatform
        const val ktorVersion = "1.4.0"
        const val coroutinesVersion = "1.3.9"

        object serialization {
            private const val version = "1.0.0-RC"
            const val plugin = "org.jetbrains.kotlin:kotlin-serialization:${kotlin.version}"
            const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:$version"
            const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0"
            const val jvm = "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.0.0"
            const val cbor = "org.jetbrains.kotlinx:kotlinx-serialization-cbor:$version"
        }

        object ktor {
            const val logging = "ch.qos.logback:logback-classic:1.2.3"
            const val serialization = "io.ktor:ktor-serialization:${ktorVersion}"
            const val websockets = "io.ktor:ktor-websockets:${ktorVersion}"

            object server {
                const val core = "io.ktor:ktor-server-core:${ktorVersion}"
                const val netty = "io.ktor:ktor-server-netty:${ktorVersion}"

                val all = arrayOf(logging, serialization, websockets, core, netty)
            }

            object client {

                const val x = "io.ktor:ktor-client-cio:${ktorVersion}"
                const val y = "io.ktor:ktor-client-logging-jvm:${ktorVersion}"
            }
        }

        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
        const val metadata = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.1.0"

        const val yaml = "com.charleskorn.kaml:kaml:0.28.3"
    }

    object test {
        const val assertj = "org.assertj:assertj-core:3.11.1"
        const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.2.8"
        const val junit = "junit:junit:4.13"
        const val ktor = "io.ktor:ktor-server-tests:${kotlin.ktorVersion}"
        const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlin.coroutinesVersion}"
        const val truth = "com.google.truth:truth:1.0"
    }
}