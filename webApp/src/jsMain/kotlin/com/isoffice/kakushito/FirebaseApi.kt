package com.isoffice.kakushito

private val fetch: dynamic = js("fetch")
private val apiBaseUrl: String = js(
    "window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' ? 'http://localhost:8080' : window.location.origin"
)

object FirebaseApi {
    fun getMe(idToken: String): dynamic {
        val options = js(
            """
            ({
                method: "GET",
                headers: {
                    Authorization: "Bearer " + idToken
                }
            })
            """
        )

        return fetch("$apiBaseUrl/api/me", options)
            .then { response ->
                if (!response.ok as Boolean) {
                    throw RuntimeException("GET /api/me failed with HTTP ${response.status}")
                }

                response.json()
            }
    }
}
