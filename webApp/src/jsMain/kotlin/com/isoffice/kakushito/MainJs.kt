package com.isoffice.kakushito

import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * 現在の Firebase セッションの ID Token を使って `/api/me` を取得し、
 * 型付き [User] としてコールバックする。
 */
fun loadAuthenticatedUser(
    onUserLoaded: (User) -> Unit,
    onError: (String) -> Unit
) {
    val idTokenPromise = FirebaseAuthManager.getIdToken()

    if (idTokenPromise == null) {
        onError("No Firebase session")
        return
    }

    idTokenPromise
        .then { idToken ->
            FirebaseApi.getMe(idToken as String)
        }
        .then { body ->
            val user = json.decodeFromString(User.serializer(), body as String)
            console.log("Authenticated user:", user)
            onUserLoaded(user)
        }
        .catch { error ->
            console.error("Loading authenticated user failed:", error)
            onError(error.message?.toString() ?: error.toString())
        }
}

fun startFirebaseLogin(onError: (String) -> Unit) {
    FirebaseAuthManager.signInWithGoogle()
        .catch { error ->
            console.error("Firebase login failed:", error)
            onError(error.message?.toString() ?: error.toString())
        }
}
