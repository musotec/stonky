buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = deps.kotlin.version))
        classpath(deps.android.gradlePlugin)
        classpath(deps.kotlin.serialization.plugin)
    }
}

group = "tech.muso.stonky"
version = "0.1.0-SNAPSHOT"

allprojects {
    repositories {
        // FIXME: disable early access preview?
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }

        jcenter()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }

        // FIXME: this should probably only be in JS...
        maven("https://kotlin.bintray.com/kotlin-js-wrappers/") // react, styled, ...
    }
}