package com.isoffice.kakushito

fun startFirebaseLogin(
    onUserLoaded: (dynamic) -> Unit,
    onError: (dynamic) -> Unit
) {
    FirebaseAuthManager.signInAndGetIdToken()
        .then { idToken ->
            FirebaseApi.getMe(idToken as String)
        }
        .then { user ->
            console.log("Authenticated user:", user)
            onUserLoaded(user)
        }
        .catch { error ->
            console.error("Firebase login failed:", error)
            onError(error)
        }
}
