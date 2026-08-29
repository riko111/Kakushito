package com.isoffice.kakushito.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    FirebaseAdmin.initialize()
    val dataSource = createDataSource()
    val userRepository = UserRepository(dataSource)

    embeddedServer(
        Netty,
        host = "0.0.0.0",
        port = 8080
    ) {
        install(CORS) {
            allowHost("localhost", schemes = listOf("http"))
            allowHost("127.0.0.1", schemes = listOf("http"))
            allowHost("192.168.1.108", schemes = listOf("http"))
            allowHost("kakushito.isoffice.com", schemes = listOf("https"))
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
        }

        install(ContentNegotiation) {
            json()
        }

        configureFirebaseAuth()

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

            get("/api/me") {
                if (!call.attributes.contains(FirebaseUserKey)) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication required")
                    )
                    return@get
                }

                val firebaseUser = call.attributes[FirebaseUserKey]

                val user = userRepository.findByFirebaseUid(firebaseUser.uid)
                    ?: userRepository.createFromFirebase(firebaseUser)

                call.respond(user)
            }
        }
    }.start(wait = true)
}
