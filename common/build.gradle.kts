@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
}

group = "tech.muso.stonky"
version = "1.0"

repositories {
    google()
}

kotlin {
    android {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
            java {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
    jvm("server") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
            java {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
    js {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {
            // NOTE: do not include any JVM dependencies in here!!
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("io.ktor:ktor-client-core:${deps.kotlin.ktorVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${deps.kotlin.coroutinesVersion}")

                implementation(deps.kotlin.serialization.core)
                implementation(deps.kotlin.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":config"))
                // Android project calls this via project(":common") and then gets these dependencies
//                implementation("io.ktor:ktor-serialization:${deps.kotlin.ktorVersion}")
//                api("androidx.appcompat:appcompat:1.2.0")
//                api("androidx.core:core-ktx:1.3.1")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(deps.test.junit)
                // TODO: androidTestLibraries
            }
        }

        val serverMain by getting {
            dependencies {
                api(project(":config")) // use API to expose Config apis to server module
            }
        }

        val serverTest by getting
        val jsMain by getting
//        val desktopMain by getting
//        val desktopTest by getting
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
}

dependencies {
    implementation(project(mapOf("path" to ":config")))
}
