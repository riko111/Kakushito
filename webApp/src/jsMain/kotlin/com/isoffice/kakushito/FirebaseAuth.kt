package com.isoffice.kakushito

@JsModule("firebase/app")
@JsNonModule
external object FirebaseAppModule {
    fun initializeApp(config: dynamic): dynamic
}

@JsModule("firebase/auth")
@JsNonModule
external object FirebaseAuthModule {
    fun getAuth(app: dynamic): dynamic

    fun signInWithPopup(
        auth: dynamic,
        provider: dynamic
    ): dynamic
}


@JsModule("firebase/auth")
@JsNonModule
external class GoogleAuthProvider {
    constructor()
}
object FirebaseAuthManager {

    private var app: dynamic = null
    private var auth: dynamic = null

    fun initialize() {
        if (app != null) {
            return
        }

        val config = js(
            """
            ({
                apiKey: "AIzaSyC7d-zsHLHIavUhAFc3fftDkrGf7A5nF6E",
                authDomain: "forkakushito.firebaseapp.com",
                projectId: "forkakushito",
                storageBucket: "forkakushito.firebasestorage.app",
                messagingSenderId: "38789095196",
                appId: "1:38789095196:web:024dffc27dcdc56dc564d9"
            })
            """
        )

        app = FirebaseAppModule.initializeApp(config)
        auth = FirebaseAuthModule.getAuth(app)
    }

    fun signInWithGoogle(): dynamic {
        initialize()

        val provider = GoogleAuthProvider()

        return FirebaseAuthModule.signInWithPopup(
            auth,
            provider
        )
    }

    fun getIdToken(): dynamic {
        initialize()

        val currentUser = auth.currentUser ?: return null

        return currentUser.getIdToken(false)
    }

    fun signInAndGetIdToken(): dynamic {
        return signInWithGoogle()
            .then { result ->
                result.user.getIdToken(false)
            }
    }
}