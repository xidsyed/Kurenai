plugins {
    kotlin("jvm") version "1.8.0"
    `maven-publish`
}

group = "org.xidsyed"
version = "0.1.0"

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



publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.xidsyed"
            artifactId = "kurunei"
            version = "0.1.0"

            from(components["java"])
        }
    }
}
