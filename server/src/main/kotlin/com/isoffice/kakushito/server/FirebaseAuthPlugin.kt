package com.isoffice.kakushito.server

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*

val FirebaseUserKey = AttributeKey<FirebaseToken>("FirebaseUser")

fun Application.configureFirebaseAuth() {

    intercept(ApplicationCallPipeline.Plugins) {
        val authorization = call.request.headers[HttpHeaders.Authorization]

        if (authorization == null) {
            return@intercept
        }

        if (!authorization.startsWith("Bearer ")) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid authorization header")
            )
            finish()
            return@intercept
        }

        val idToken = authorization.removePrefix("Bearer ").trim()

        if (idToken.isEmpty()) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Missing Firebase ID token")
            )
            finish()
            return@intercept
        }

        try {
            val decodedToken = FirebaseAuth.getInstance()
                .verifyIdToken(idToken)

            call.attributes.put(FirebaseUserKey, decodedToken)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid Firebase ID token")
            )
            finish()
        }
    }
}