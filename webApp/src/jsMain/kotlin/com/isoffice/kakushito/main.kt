package com.isoffice.kakushito

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        DisposableEffect(Unit) {
            val stopObserving = FirebaseAuthManager.observeAuthState { firebaseUser ->
                if (firebaseUser == null) {
                    CurrentUser.setSignedOut()
                } else {
                    CurrentUser.setLoading()
                    loadAuthenticatedUser(
                        onUserLoaded = { user -> CurrentUser.setSignedIn(user) },
                        onError = { message -> CurrentUser.setError(message) }
                    )
                }
            }

            onDispose(stopObserving)
        }

        App(
            authState = CurrentUser.state,
            onGoogleLogin = {
                CurrentUser.setLoading()
                startFirebaseLogin(
                    onError = { message -> CurrentUser.setError(message) }
                )
            },
            onGoogleLogout = {
                FirebaseAuthManager.signOut()
                    .catch { error ->
                        CurrentUser.setError(error.message?.toString() ?: error.toString())
                    }
            }
        )
    }
}
