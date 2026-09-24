package com.gptplus18.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

/**
 * تخزين بيانات المستخدم (Token + Name + Email + UID)
 * ⭐ Dual Storage: DataStore + SharedPreferences للنسخ الاحتياطي
 * يضمن عدم فقدان الجلسة عند التحديث أو أي عطل في DataStore
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val KEY_TOKEN = stringPreferencesKey("auth_token")
    private val KEY_NAME = stringPreferencesKey("auth_name")
    private val KEY_EMAIL = stringPreferencesKey("auth_email")
    private val KEY_UID = longPreferencesKey("auth_uid")

    // ⭐ Backup store — SharedPreferences منفصل
    private val backupPrefs: SharedPreferences =
        context.getSharedPreferences("auth_backup", Context.MODE_PRIVATE)

    private fun backupSave(token: String, name: String, email: String, uid: Long) {
        backupPrefs.edit()
            .putString("token", token)
            .putString("name", name)
            .putString("email", email)
            .putLong("uid", uid)
            .apply()
    }

    private fun backupClear() {
        backupPrefs.edit().clear().apply()
    }

    // ─── الحفظ ───
    suspend fun save(token: String, name: String, email: String, uid: Long) {
        // 1) نخزن في DataStore
        try {
            context.authDataStore.edit { prefs ->
                prefs[KEY_TOKEN] = token
                prefs[KEY_NAME] = name
                prefs[KEY_EMAIL] = email
                prefs[KEY_UID] = uid
            }
        } catch (_: Exception) {
            // تجاهل — عندنا backup
        }
        // 2) نخزن في SharedPreferences (نسخة احتياطية دائماً)
        backupSave(token, name, email, uid)
    }

    // ─── القراءة مع fallback ───
    suspend fun getToken(): String? {
        // جرّب DataStore أولاً
        val ds = try {
            context.authDataStore.data.map { it[KEY_TOKEN] }.first()
        } catch (_: Exception) {
            null
        }
        if (!ds.isNullOrBlank()) return ds

        // fallback: من SharedPreferences
        val backup = backupPrefs.getString("token", null)
        if (!backup.isNullOrBlank()) {
            // ✅ نرجع التوكن ونعيد كتابته في DataStore
            try {
                context.authDataStore.edit { prefs ->
                    prefs[KEY_TOKEN] = backup
                    backupPrefs.getString("name", null)?.let { prefs[KEY_NAME] = it }
                    backupPrefs.getString("email", null)?.let { prefs[KEY_EMAIL] = it }
                    val u = backupPrefs.getLong("uid", 0L)
                    if (u > 0) prefs[KEY_UID] = u
                }
            } catch (_: Exception) { /* تجاهل */ }
            return backup
        }
        return null
    }

    suspend fun getName(): String? {
        val ds = try {
            context.authDataStore.data.map { it[KEY_NAME] }.first()
        } catch (_: Exception) { null }
        return ds ?: backupPrefs.getString("name", null)
    }

    suspend fun getEmail(): String? {
        val ds = try {
            context.authDataStore.data.map { it[KEY_EMAIL] }.first()
        } catch (_: Exception) { null }
        return ds ?: backupPrefs.getString("email", null)
    }

    suspend fun getUid(): Long {
        val ds = try {
            context.authDataStore.data.map { it[KEY_UID] ?: 0L }.first()
        } catch (_: Exception) { 0L }
        if (ds > 0) return ds
        return backupPrefs.getLong("uid", 0L)
    }

    suspend fun hasToken(): Boolean = !getToken().isNullOrBlank()

    // ─── الحذف ───
    suspend fun clear() {
        try {
            context.authDataStore.edit { it.clear() }
        } catch (_: Exception) { /* تجاهل */ }
        backupClear()
    }
}
