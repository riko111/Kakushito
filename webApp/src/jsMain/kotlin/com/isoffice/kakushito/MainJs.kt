package com.isoffice.kakushito

import kotlinx.browser.window
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

private const val productionHost = "kakushito.isoffice.com"

private fun apiBaseUrl(): String {
    val location = window.location

    return if (location.hostname == productionHost) {
        "${location.protocol}//${location.host}"
    } else {
        "${location.protocol}//${location.hostname}:8080"
    }
}

fun startFirebaseLogin() {
    FirebaseAuthManager.signInAndGetIdToken()
        .then { idToken ->
            val headers = Headers().apply {
                append("Authorization", "Bearer $idToken")
            }

            window.fetch(
                "${apiBaseUrl()}/api/me",
                RequestInit(headers = headers)
            )
        }
        .then { response ->
            if (!response.ok) {
                throw RuntimeException("API /api/me failed: HTTP ${response.status}")
            }
            response.text()
        }
        .then { body ->
            console.log("API /api/me:", body)
        }
        .catch { error ->
            console.error("Firebase/API sign-in failed:", error)
        }
}
