package com.gptplus18.app.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object GoogleAuthLauncher {
    private const val AUTH_URL = "https://gptplus18.com/api/auth/google/login?mobile=1"

    fun launch(context: Context) {
        try {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .build()

            intent.intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.launchUrl(context, Uri.parse(AUTH_URL))
        } catch (e: Exception) {
            // fallback: فتح في المتصفح العادي
            try {
                val fallback = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse(AUTH_URL),
                )
                fallback.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
            } catch (_: Exception) { }
        }
    }
}
