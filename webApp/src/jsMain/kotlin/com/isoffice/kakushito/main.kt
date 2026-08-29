package com.isoffice.kakushito

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        var loginStatus by remember { mutableStateOf("Not signed in") }

        App(
            onGoogleLogin = {
                loginStatus = "Signing in..."
                startFirebaseLogin(
                    onUserLoaded = { user ->
                        loginStatus = "Signed in as ${user.email ?: user.firebaseUid}"
                    },
                    onError = { error ->
                        loginStatus = "Sign-in failed: ${error.message ?: error}"
                    }
                )
            },
            loginStatus = loginStatus
        )
    }
}
