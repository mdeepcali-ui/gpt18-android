package com.gptplus18.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gptplus18.app.data.repository.NotificationsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmEntryPoint {
    fun notificationsRepo(): NotificationsRepository
}

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID = "gptplus18_default"
        private const val CHANNEL_NAME = "إشعارات GPT+18"
        private const val CHANNEL_DESC = "إشعارات التطبيق العامة"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔥 FCM Token جديد: ${token.take(30)}...")

        // نخزنو محلياً
        getSharedPreferences(NotificationsRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(NotificationsRepository.KEY_FCM_TOKEN, token)
            .apply()

        // ✨ نبعثو للسيرفر عبر NotificationsRepository
        try {
            val entry = EntryPointAccessors.fromApplication(
                applicationContext,
                FcmEntryPoint::class.java,
            )
            val repo = entry.notificationsRepo()
            CoroutineScope(Dispatchers.IO).launch {
                repo.registerCurrentToken()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل إرسال التوكن للسيرفر: ${e.message}")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "📬 رسالة جديدة من: ${message.from}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "GPT+18"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val deepLink = message.data["deep_link"]

        Log.d(TAG, "   Title: $title")
        Log.d(TAG, "   Data: ${message.data}")

        if (title.isNotBlank() || body.isNotBlank()) {
            showNotification(title, body, deepLink)
        }
    }

    private fun showNotification(title: String, body: String, deepLink: String?) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deepLink?.let { putExtra("deep_link", it) }
        }

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingFlags,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(
                System.currentTimeMillis().toInt(),
                notification,
            )
            Log.d(TAG, "✅ تم عرض الإشعار")
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ ما في permission: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CHANNEL_DESC
            enableLights(true)
            enableVibration(true)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
