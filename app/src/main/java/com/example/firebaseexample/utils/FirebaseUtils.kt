package com.example.firebaseexample.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseUtils {
    fun initializeFirebase(appContext: Context) {
        val options = FirebaseOptions.Builder()
            .setApplicationId("your-app-id")
            .setApiKey("your-api-key")
            .setDatabaseUrl("your-database-url")
            .build()
        FirebaseApp.initializeApp(appContext, options)
    }
}
