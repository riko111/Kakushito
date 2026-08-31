package com.isoffice.kakushito

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        var loginStatus by remember { mutableStateOf("Checking sign-in status...") }
        var isLoggedIn by remember { mutableStateOf(false) }

        DisposableEffect(Unit) {
            val stopObserving = FirebaseAuthManager.observeAuthState { firebaseUser ->
                if (firebaseUser == null) {
                    isLoggedIn = false
                    loginStatus = "Not signed in"
                } else {
                    loginStatus = "Loading account..."
                    loadAuthenticatedUser(
                        onUserLoaded = { user ->
                            isLoggedIn = true
                            loginStatus = "Signed in as ${user.email ?: user.firebaseUid}"
                        },
                        onError = { error ->
                            isLoggedIn = false
                            loginStatus = "Could not load account: ${error.message ?: error}"
                        }
                    )
                }
            }

            onDispose(stopObserving)
        }

        App(
            onGoogleLogin = {
                loginStatus = "Signing in..."
                startFirebaseLogin(
                    onError = { error ->
                        isLoggedIn = false
                        loginStatus = "Sign-in failed: ${error.message ?: error}"
                    }
                )
            },
            onGoogleLogout = {
                FirebaseAuthManager.signOut()
                    .catch { error ->
                        loginStatus = "Sign-out failed: ${error.message ?: error}"
                    }
            },
            loginStatus = loginStatus,
            isLoggedIn = isLoggedIn
        )
    }
}
