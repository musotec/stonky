import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm")
    kotlin("plugin.spring") version "1.4.32"
    id("com.avast.gradle.docker-compose")
}

group = "tech.muso.stonky.repository"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Lettuce > Jedis for async applications.
    implementation("io.lettuce:lettuce-core:6.1.1.RELEASE")

    // do everything in JSON first for debugging // TODO: remove and use byte serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")

    // springfox 3.0.0+ required for webflux support
    implementation("io.springfox:springfox-boot-starter:3.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dockerCompose {
    useComposeFiles = listOf("docker-compose.yml")
//	upAdditionalArgs = listOf("-V redis.conf:/usr/local/etc/redis/redis.conf")
}