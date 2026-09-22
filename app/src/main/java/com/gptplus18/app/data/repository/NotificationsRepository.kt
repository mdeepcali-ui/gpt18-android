package com.gptplus18.app.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.RegisterTokenRequest
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مسؤول عن إدارة FCM Token
 *  1. جلب الـ token من Firebase
 *  2. إرسالو للسيرفر (عشان السيرفر يبعت إشعارات لهاد المستخدم)
 */
@Singleton
class NotificationsRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val TAG = "NotificationsRepo"
        const val PREFS_NAME = "fcm_prefs"
        const val KEY_FCM_TOKEN = "fcm_token"
    }

    /**
     * يجيب FCM token الحالي من Firebase، ويبعثو للسيرفر
     * @return true إذا نجح
     */
    suspend fun registerCurrentToken(): Boolean {
        return try {
            // 1. نجيب الـ auth token (من DataStore)
            val authToken = tokenStorage.getToken()
            if (authToken.isNullOrBlank()) {
                Log.d(TAG, "⏭️ ما في auth token — المستخدم مو مسجل")
                return false
            }

            // 2. نجيب FCM token من Firebase
            val fcmToken = getFcmToken() ?: return false
            Log.d(TAG, "🔥 FCM Token: ${fcmToken.take(30)}...")

            // 3. نبعثو للسيرفر
            val response = api.registerFcmToken(
                bearer = "Bearer $authToken",
                body = RegisterTokenRequest(fcmToken),
            )

            if (response.isSuccessful) {
                Log.d(TAG, "✅ تم تسجيل FCM token على السيرفر")
                // نخزنو محلياً
                saveLocal(fcmToken)
                true
            } else {
                Log.e(TAG, "❌ السيرفر رفض: HTTP ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل الاتصال: ${e.message}")
            false
        }
    }

    /**
     * يجيب FCM token من Firebase (بشكل suspend)
     */
    suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل جلب FCM token: ${e.message}")
            null
        }
    }

    /**
     * يخزن الـ token محلياً (للوصول السريع)
     */
    private fun saveLocal(token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }

    /**
     * يرجع الـ token المخزن محلياً (بدون اتصال بـ Firebase)
     */
    fun getLocalToken(): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
    }
}
