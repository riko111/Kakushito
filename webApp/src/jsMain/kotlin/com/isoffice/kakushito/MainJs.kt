package com.isoffice.kakushito

fun startFirebaseLogin() {
    FirebaseAuthManager.signInAndGetUid()
        .then { uid ->
            console.log("Firebase Uid:", uid)
        }
        .catch { error ->
            console.error("Firebase sign-in failed:", error)
        }
}