package com.isoffice.kakushito

import kotlinx.browser.window
import org.w3c.fetch.Headers

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

            // Build RequestInit dynamically so Kotlin/JS does not emit
            // optional RequestInit fields with null values. Chrome rejects
            // null for enum-valued fields such as RequestInit.cache.
            val requestInit: dynamic = js("({})")
            requestInit.headers = headers

            window.fetch(
                "${apiBaseUrl()}/api/me",
                requestInit
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
