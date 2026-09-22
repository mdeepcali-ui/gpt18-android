package com.gptplus18.app.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مسؤول عن تسجيل الأحداث في Firebase Analytics
 *
 * الأحداث المتتبَّعة:
 *  - signup / login / logout
 *  - chat_start / message_sent / image_generated / song_generated / video_generated
 *  - subscription_started / payment_verified
 */
@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val TAG = "Analytics"

        // ─── أسماء الأحداث ───
        const val EVENT_SIGNUP = "signup"
        const val EVENT_LOGIN = "login"
        const val EVENT_LOGOUT = "logout"
        const val EVENT_CHAT_START = "chat_start"
        const val EVENT_MESSAGE_SENT = "message_sent"
        const val EVENT_IMAGE_GENERATED = "image_generated"
        const val EVENT_SONG_GENERATED = "song_generated"
        const val EVENT_VIDEO_GENERATED = "video_generated"
        const val EVENT_SUBSCRIPTION_STARTED = "subscription_started"
        const val EVENT_PAYMENT_VERIFIED = "payment_verified"
        const val EVENT_CODE_JOB_STARTED = "code_job_started"

        // ─── مفاتيح ───
        const val KEY_METHOD = "method"
        const val KEY_PLAN = "plan"
        const val KEY_NETWORK = "network"
        const val KEY_PRESET = "preset"
        const val KEY_DURATION = "duration"
        const val KEY_MODEL = "model"
        const val KEY_SUCCESS = "success"
    }

    private val analytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    // ═══════════════════════════════════════════
    // Auth Events
    // ═══════════════════════════════════════════

    fun logSignup(method: String = "email") {
        logEvent(EVENT_SIGNUP, Bundle().apply {
            putString(KEY_METHOD, method)
        })
    }

    fun logLogin(method: String = "email") {
        logEvent(EVENT_LOGIN, Bundle().apply {
            putString(KEY_METHOD, method)
        })
    }

    fun logLogout() {
        logEvent(EVENT_LOGOUT, Bundle())
    }

    // ═══════════════════════════════════════════
    // Chat Events
    // ═══════════════════════════════════════════

    fun logChatStart() {
        logEvent(EVENT_CHAT_START, Bundle())
    }

    fun logMessageSent(length: Int) {
        logEvent(EVENT_MESSAGE_SENT, Bundle().apply {
            putInt("length", length)
        })
    }

    // ═══════════════════════════════════════════
    // Media Events
    // ═══════════════════════════════════════════

    fun logImageGenerated(preset: String) {
        logEvent(EVENT_IMAGE_GENERATED, Bundle().apply {
            putString(KEY_PRESET, preset)
        })
    }

    fun logSongGenerated(durationSec: Int) {
        logEvent(EVENT_SONG_GENERATED, Bundle().apply {
            putInt(KEY_DURATION, durationSec)
        })
    }

    fun logVideoGenerated(durationSec: Int, model: String) {
        logEvent(EVENT_VIDEO_GENERATED, Bundle().apply {
            putInt(KEY_DURATION, durationSec)
            putString(KEY_MODEL, model)
        })
    }

    // ═══════════════════════════════════════════
    // Subscription Events
    // ═══════════════════════════════════════════

    fun logSubscriptionStarted(plan: String) {
        logEvent(EVENT_SUBSCRIPTION_STARTED, Bundle().apply {
            putString(KEY_PLAN, plan)
        })
    }

    fun logPaymentVerified(plan: String, network: String, success: Boolean) {
        logEvent(EVENT_PAYMENT_VERIFIED, Bundle().apply {
            putString(KEY_PLAN, plan)
            putString(KEY_NETWORK, network)
            putBoolean(KEY_SUCCESS, success)
        })
    }

    // ═══════════════════════════════════════════
    // Code Agent Events
    // ═══════════════════════════════════════════

    fun logCodeJobStarted(model: String) {
        logEvent(EVENT_CODE_JOB_STARTED, Bundle().apply {
            putString(KEY_MODEL, model)
        })
    }

    // ═══════════════════════════════════════════
    // Internal
    // ═══════════════════════════════════════════

    private fun logEvent(name: String, params: Bundle) {
        try {
            analytics.logEvent(name, params)
            Log.d(TAG, "📊 $name")
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل تسجيل $name: ${e.message}")
        }
    }
}
