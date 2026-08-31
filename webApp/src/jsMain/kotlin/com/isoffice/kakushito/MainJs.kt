package com.isoffice.kakushito

fun loadAuthenticatedUser(
    onUserLoaded: (dynamic) -> Unit,
    onError: (dynamic) -> Unit
) {
    FirebaseAuthManager.getIdToken()
        ?.then { idToken ->
            FirebaseApi.getMe(idToken as String)
        }
        ?.then { user ->
            console.log("Authenticated user:", user)
            onUserLoaded(user)
        }
        ?.catch { error ->
            console.error("Loading authenticated user failed:", error)
            onError(error)
        }
}

fun startFirebaseLogin(onError: (dynamic) -> Unit) {
    FirebaseAuthManager.signInWithGoogle()
        .catch { error ->
            console.error("Firebase login failed:", error)
            onError(error)
        }
}
