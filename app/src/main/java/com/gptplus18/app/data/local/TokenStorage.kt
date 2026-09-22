package com.gptplus18.app.data.local

import android.content.Context
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
 * يستخدم DataStore — يشتغل بسلاسة مع الـ Coroutines
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val KEY_TOKEN = stringPreferencesKey("auth_token")
    private val KEY_NAME = stringPreferencesKey("auth_name")
    private val KEY_EMAIL = stringPreferencesKey("auth_email")
    private val KEY_UID = longPreferencesKey("auth_uid")

    // ─── الحفظ ───
    suspend fun save(token: String, name: String, email: String, uid: Long) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_NAME] = name
            prefs[KEY_EMAIL] = email
            prefs[KEY_UID] = uid
        }
    }

    // ─── القراءة ───
    suspend fun getToken(): String? =
        context.authDataStore.data.map { it[KEY_TOKEN] }.first()

    suspend fun getName(): String? =
        context.authDataStore.data.map { it[KEY_NAME] }.first()

    suspend fun getEmail(): String? =
        context.authDataStore.data.map { it[KEY_EMAIL] }.first()

    suspend fun getUid(): Long =
        context.authDataStore.data.map { it[KEY_UID] ?: 0L }.first()

    suspend fun hasToken(): Boolean = !getToken().isNullOrBlank()

    // ─── الحذف ───
    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}
