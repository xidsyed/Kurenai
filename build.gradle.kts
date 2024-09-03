plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "dpr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC")
    implementation("de.m3y.kformat:kformat:0.11")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}