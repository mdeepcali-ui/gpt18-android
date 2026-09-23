package com.gptplus18.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ DT: يعطي معرّفاً فريداً للجهاز (لا يحتاج إذن)
 * يستخدم Android ID + نموذج الجهاز + بصمة البناء
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var cached: String? = null

    @SuppressLint("HardwareIds")
    fun get(): String {
        cached?.let { return it }
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (_: Exception) { "" }

        val raw = "${androidId}|${Build.MANUFACTURER}|${Build.MODEL}|${Build.FINGERPRINT}"
        val hash = try {
            val md = MessageDigest.getInstance("SHA-256")
            md.digest(raw.toByteArray()).joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            raw.hashCode().toString()
        }
        cached = hash.take(32)
        return cached!!
    }
}
