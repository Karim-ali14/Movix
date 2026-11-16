package com.karimali.movix

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovixApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().subscribeToTopic("movies")
            .addOnSuccessListener { Log.d("FCM", "Subscribed to topic movies") }
            .addOnFailureListener { Log.w("FCM", "Topic subscription failed", it) }
    }
}