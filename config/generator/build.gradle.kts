plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.squareup:kotlinpoet:1.8.0")

    implementation(deps.kotlin.serialization.core)
    implementation(deps.kotlin.serialization.jvm)

    // kotlin yaml parsing library
    implementation(deps.kotlin.yaml)
}

// task to generate the Config object from the config.yaml
tasks.register<JavaExec>("generate") {
    group = "kotlinpoet"
    classpath = sourceSets["main"].runtimeClasspath
    main = "YamlConfigGeneratorKt"
}