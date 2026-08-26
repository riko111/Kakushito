package com.isoffice.kakushito.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(
        Netty,
        host = "0.0.0.0",
        port = 8080
    ) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/health") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("status" to "ok")
                )
            }
        }
    }.start(wait = true)
}