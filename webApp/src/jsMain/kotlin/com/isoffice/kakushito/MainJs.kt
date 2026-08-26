package com.isoffice.kakushito

fun startFirebaseLogin() {
    FirebaseAuthManager.signInAndGetIdToken()
        .then { token ->
            console.log("Firebase ID Token:", token)
        }
        .catch { error ->
            console.error("Firebase sign-in failed:", error)
        }
}