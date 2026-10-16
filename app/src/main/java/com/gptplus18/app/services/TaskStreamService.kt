package com.gptplus18.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * TaskStreamService — Foreground Service يبقي الـ stream حيّاً.
 *
 * - يفتح SSE على /api/code/task/{task_id}
 * - يخزّن chunks في TaskStreamManager (singleton)
 * - يبثها للـ ViewModel عبر Flow
 * - يظهر إشعار دائم حتى انتهاء المهمة
 * - يكمل لما المستخدم يطلع من التطبيق
 */
class TaskStreamService : Service() {

    companion object {
        private const val TAG = "TaskStreamService"
        private const val CHANNEL_ID = "gpt18_task_stream"
        private const val CHANNEL_NAME = "جاري التوليد"
        private const val NOTIF_ID = 8432

        const val ACTION_START = "com.gptplus18.app.START_TASK_STREAM"
        const val ACTION_STOP = "com.gptplus18.app.STOP_TASK_STREAM"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_TITLE = "title"

        fun start(context: Context, taskId: String, sessionId: Int, title: String = "") {
            val i = Intent(context, TaskStreamService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_TITLE, title)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            val i = Intent(context, TaskStreamService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(i)
        }
    }

    private var currentTaskId: String? = null
    private var currentSessionId: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
                val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, 0)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: ""

                currentTaskId = taskId
                currentSessionId = sessionId
                Log.d(TAG, "START task_id=$taskId session=$sessionId")

                // إشعار دائم
                startForeground(NOTIF_ID, buildNotification("🤖 جاري التوليد..."))

                // نشغل الـ stream
                TaskStreamManager.startTask(
                    context = applicationContext,
                    taskId = taskId,
                    sessionId = sessionId,
                    onDone = { stopSelf() },
                )
            }
            ACTION_STOP -> {
                Log.d(TAG, "STOP requested")
                TaskStreamManager.stopTask()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "يعرض حالة المهام الجارية"
            setShowBadge(false)
        }
        mgr.createNotificationChannel(ch)
    }

    private fun buildNotification(text: String): android.app.Notification {
        val intent = Intent(this, Class.forName("com.gptplus18.app.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("deep_link", "code")
            putExtra("task_id", currentTaskId ?: "")
            if (currentSessionId > 0) putExtra("session_id", currentSessionId)
        }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT

        val pi = PendingIntent.getActivity(this, 0, intent, pendingFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("GPT+18")
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pi)
            .build()
    }
}
