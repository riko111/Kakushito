package com.isoffice.kakushito

private val fetch: dynamic = js("fetch")

// 本番は API と同一オリジンで配信される想定。
// 開発時は Kotlin/JS の webpack dev server (8081/8082) からアクセスされるため、
// 同じホストの API サーバ (8080) を向ける。ホスト名固定ではなく location 由来にして
// localhost / 127.0.0.1 / LAN IP (別PCからのアクセス) すべてに対応する。
private val apiBaseUrl: String = js(
    """
    (function () {
        var loc = window.location;
        if (loc.port === "8081" || loc.port === "8082") {
            return loc.protocol + "//" + loc.hostname + ":8080";
        }
        return loc.origin;
    })()
    """
)

object FirebaseApi {
    /** `GET /api/me` を呼び、レスポンスボディ(JSON文字列)で解決する Promise を返す。 */
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

                response.text()
            }
    }
}
