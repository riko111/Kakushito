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
    FirebaseAdmin.initialize()
    val dataSource = createDataSource()

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

            get("/api/health/db") {
                dataSource.connection.use { connection ->
                    connection.createStatement().use { statement ->
                        statement.executeQuery("SELECT DATABASE()").use { result ->
                            result.next()

                            call.respond(
                                HttpStatusCode.OK,
                                mapOf(
                                    "status" to "ok",
                                    "database" to result.getString(1)
                                )
                            )
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}