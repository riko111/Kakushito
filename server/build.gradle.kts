plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.4.10"
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
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.mysql.connector)
    implementation(libs.hikari)
    implementation(libs.firebase.admin)

    testImplementation(libs.ktor.server.test.host)
}

kotlin {
    jvmToolchain(21)
}
