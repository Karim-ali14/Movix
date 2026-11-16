package com.karimali.movix

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovixApp : Application() {

    override fun onCreate() {
        super.onCreate()
        subscribeToMovieUpdates()
    }

    /**
     * Push is optional: without google-services.json (or in unit tests) Firebase is not
     * initialized, and the app must still start.
     */
    private fun subscribeToMovieUpdates() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            Log.w(TAG, "Firebase is not configured; push updates are disabled")
            return
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
            .addOnSuccessListener { Log.d(TAG, "Subscribed to topic $TOPIC") }
            .addOnFailureListener { Log.w(TAG, "Topic subscription failed", it) }
    }

    private companion object {
        const val TAG = "FCM"
        const val TOPIC = "movies"
    }
}