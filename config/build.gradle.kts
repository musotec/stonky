plugins {
    `java-library`
    kotlin("jvm")
}

sourceSets {
    main {
        java {
            // force the source directory to be the generated build folder.
            setSrcDirs(listOf("build/generated/src/main"))
        }
    }
}

// execute the generator (kotlinpoet) to generate the source file
tasks.getByName("compileKotlin") {
    dependsOn(":config:generator:generate")
}
