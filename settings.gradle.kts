pluginManagement {
    repositories {
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2/") }

        google()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven("https://kotlin.bintray.com/kotlin-js-wrappers/") // react, styled, ...
    }
    
}

rootProject.name = "stonky"

include(":common")
include(":repository")
include(":server")
include(":config")
include(":config:generator")

include(":android:app")
include(":android:repos:service")
include(":android:graph:core")
include(":android:graph:spark")
include(":android:graph:graphly")
include(":android:theme")
include(":android:repos")
include(":android:database")
include(":android:core")
