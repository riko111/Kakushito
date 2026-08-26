package com.isoffice.kakushito.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.FileInputStream

object FirebaseAdmin {

    private const val CREDENTIALS_PATH =
        "/etc/kakushito/forkakushito-firebase-adminsdk-fbsvc-8bc16ea867.json"

    fun initialize(): FirebaseApp {
        if (FirebaseApp.getApps().isNotEmpty()) {
            return FirebaseApp.getInstance()
        }

        FileInputStream(CREDENTIALS_PATH).use { input ->
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(input))
                .build()

            return FirebaseApp.initializeApp(options)
        }
    }

    fun auth(): FirebaseAuth {
        initialize()
        return FirebaseAuth.getInstance()
    }
}