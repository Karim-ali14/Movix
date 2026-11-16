package com.karimali.movix.core.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karimali.movix.MainActivity
import com.karimali.movix.R
import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.features.details.domain.usecase.RefreshMovieDetailsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Receives the backend's data message, refreshes the edited movie into Room
 * (the UI observes Room, so it updates by itself) and then shows a notification
 * with the movie title and poster.
 */
@AndroidEntryPoint
class MovieMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var refreshMovieDetails: RefreshMovieDetailsUseCase

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Message received: ${message.data}")
        if (message.data[KEY_TYPE] != TYPE_MOVIE_UPDATED) return
        val movieId = message.data[KEY_MOVIE_ID]?.toIntOrNull() ?: return
        val title = message.data[KEY_MOVIE_TITLE].orEmpty()
        val posterPath = message.data[KEY_POSTER_PATH]

        // FCM calls this on a worker thread and allows about 20 seconds, so blocking here is fine.
        runBlocking {
            withTimeoutOrNull(REFRESH_TIMEOUT_MS) { refreshMovieDetails(movieId) }
        }?.onFailure { Log.w(TAG, "Refresh failed for movie $movieId", it) }
        showNotification(movieId, title, posterPath)
    }

    private fun showNotification(movieId: Int, title: String, posterPath: String?) {
        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!canNotify) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted, skipping the notification")
            return
        }

        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }

        val openApp = PendingIntent.getActivity(
            this,
            movieId,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val contentTitle = getString(R.string.notification_title)
        val contentText = getString(R.string.notification_body_format, title)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val posterBitmap = loadPoster(posterPath)

        posterBitmap?.let { bitmap ->
            val bigPictureStyle = NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .bigLargeIcon(null as Bitmap?)
                .setSummaryText(contentText)

            builder.setStyle(bigPictureStyle)
        }

        manager.notify(movieId, builder.build())
    }

    /** Downloads the poster; returns null on any failure so the notification still shows without it. */
    private fun loadPoster(posterPath: String?): Bitmap? {
        if (posterPath.isNullOrBlank()) return null
        val request = ImageRequest.Builder(this)
            .data(AppRouter.POSTER_BASE_URL + posterPath)
            .allowHardware(false) // notifications need a software bitmap
            .build()
        val result = runBlocking {
            withTimeoutOrNull(POSTER_TIMEOUT_MS) { applicationContext.imageLoader.execute(request) }
        }
        return (result as? SuccessResult)?.drawable?.toBitmap()
    }

    private companion object {
        const val TAG = "FCM"
        const val CHANNEL_ID = "movie_updates"
        const val KEY_TYPE = "type"
        const val KEY_MOVIE_ID = "movieId"
        const val KEY_MOVIE_TITLE = "movieTitle"
        const val KEY_POSTER_PATH = "posterPath"
        const val TYPE_MOVIE_UPDATED = "movie_updated"
        const val REFRESH_TIMEOUT_MS = 10_000L
        const val POSTER_TIMEOUT_MS = 5_000L
    }
}