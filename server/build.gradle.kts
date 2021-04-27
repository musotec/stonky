import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform")
    application //to run JVM part
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "tech.muso.stonky.server"
version = "0.1.0-SNAPSHOT"
dependencies {
    implementation(project(mapOf("path" to ":common")))
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":config"))
                implementation(project(":common"))

                implementation(deps.kotlin.ktor.logging)
                implementation(deps.kotlin.serialization.jvm)
                deps.kotlin.ktor.server.all.forEach { implementation(it) }


//                implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.1.1")


                // TD Ameritrade API for server data sourcing.
                implementation("com.studerw.tda:td-ameritrade-client:2.4.0")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(deps.test.ktor)
                implementation(deps.test.junit)
            }
        }

        /**
         * Dependencies for compiling the Kotlin/JS + React code for the web frontend the server hosts
         */
        val jsMain by getting {
            dependencies {
                implementation(project(":common"))

                implementation("io.ktor:ktor-client-js:${deps.kotlin.ktorVersion}") //include http&websockets

                //ktor client js json
                implementation("io.ktor:ktor-client-json-js:${deps.kotlin.ktorVersion}")
                implementation("io.ktor:ktor-client-serialization-js:${deps.kotlin.ktorVersion}")

                implementation("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.4.0")
                implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.110-kotlin-1.4.0")
                implementation(npm("react", "16.13.1"))
                implementation(npm("react-dom", "16.13.1"))
            }
        }
    }
}

application {
    mainClass.set("ServerKt")
    mainClassName = "ServerKt"  // deprecated setter needed for ShadowJar
}

// include JS artifacts in any JAR we generate
tasks.getByName<Jar>("jvmJar") {    // FIXME: runs during android builds
    val taskName = if (project.hasProperty("isProduction")) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) // bring output file along into the JAR
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}

// Alias "installDist" as "stage" (for cloud providers)
tasks.create("stage") {
    dependsOn(tasks.getByName("installDist"))
}

tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    // include the generated javascript into the jar.
    from("$buildDir/distributions")
}

tasks.getByName<Jar>("jvmJar") {
    manifest {
        attributes["Main-Class"] = "ServerKt"
    }
}

tasks.getByName<JavaExec>("run") {
    classpath(tasks.getByName<Jar>("jvmJar")) // so that the JS artifacts generated by `jvmJar` can be found and served
}