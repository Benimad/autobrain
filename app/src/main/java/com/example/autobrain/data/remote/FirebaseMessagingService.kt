package com.example.autobrain.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.autobrain.MainActivity
import com.example.autobrain.R
import com.example.autobrain.core.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService as FcmService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FcmService() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestore.collection("users")
                .document(it)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationType = message.data["type"] ?: "default"
        val title = message.notification?.title ?: "AUTO BRAIN"
        val body = message.notification?.body ?: ""
        val targetScreen = message.data["targetScreen"]
        val dataId = message.data["dataId"]

        when (notificationType) {
            "reminder" -> showNotification(
                title, body, 
                Constants.CHANNEL_ID_REMINDERS,
                "reminders",
                dataId
            )
            "ai_score" -> showNotification(
                title, body,
                Constants.CHANNEL_ID_MESSAGES,
                "ai_scores",
                dataId
            )
            "diagnostic" -> showNotification(
                title, body,
                Constants.CHANNEL_ID_MESSAGES,
                "diagnostics",
                dataId
            )
            "maintenance" -> showNotification(
                title, body,
                Constants.CHANNEL_ID_REMINDERS,
                "maintenance",
                dataId
            )
            else -> showNotification(
                title, body,
                Constants.CHANNEL_ID_MESSAGES,
                targetScreen,
                dataId
            )
        }
    }

    private fun showNotification(
        title: String, 
        body: String, 
        channelId: String,
        targetScreen: String? = null,
        dataId: String? = null
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getChannelName(channelId),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getChannelDescription(channelId)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            targetScreen?.let { putExtra("navigate_to", it) }
            dataId?.let { putExtra("data_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (dataId ?: System.currentTimeMillis().toString()).hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        notificationManager.notify(
            (dataId ?: System.currentTimeMillis().toString()).hashCode(), 
            notification
        )
    }

    private fun getChannelName(channelId: String): String {
        return when (channelId) {
            Constants.CHANNEL_ID_MESSAGES -> "Messages"
            Constants.CHANNEL_ID_BOOKINGS -> "Réservations"
            Constants.CHANNEL_ID_REMINDERS -> "Rappels"
            Constants.CHANNEL_ID_BREAKDOWN -> "Assistance panne"
            else -> "Notifications"
        }
    }

    private fun getChannelDescription(channelId: String): String {
        return when (channelId) {
            Constants.CHANNEL_ID_MESSAGES -> "Notifications de nouveaux messages"
            Constants.CHANNEL_ID_BOOKINGS -> "Notifications de réservations"
            Constants.CHANNEL_ID_REMINDERS -> "Rappels d'entretien"
            Constants.CHANNEL_ID_BREAKDOWN -> "Notifications d'assistance panne"
            else -> "Notifications générales"
        }
    }
}
