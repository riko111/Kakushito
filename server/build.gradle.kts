plugins {
    kotlin("jvm")
    id("io.ktor.plugin") version "3.5.2"
    application
}

group = "com.isoffice.kakushito"
version = "1.0.0"

application {
    mainClass.set("com.isoffice.kakushito.server.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.mysql.connector)
    implementation(libs.hikari)

    testImplementation(libs.ktor.server.test.host)
}

kotlin {
    jvmToolchain(21)
}