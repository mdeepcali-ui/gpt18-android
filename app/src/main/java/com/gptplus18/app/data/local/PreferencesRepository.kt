package com.gptplus18.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prefsDataStore by preferencesDataStore(name = "app_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val KEY_DARK = booleanPreferencesKey("dark_mode")
    private val KEY_LANG = stringPreferencesKey("app_lang")
    private val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
    private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
    private val KEY_CODE_MODEL = stringPreferencesKey("code_model")
    private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")

    val darkModeFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_DARK] ?: true }
    val langFlow: Flow<String> = context.prefsDataStore.data.map { it[KEY_LANG] ?: "ar" }
    val fontScaleFlow: Flow<Float> = context.prefsDataStore.data.map { it[KEY_FONT_SCALE] ?: 1.0f }
    val onboardingDoneFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_ONBOARDING] ?: false }
    val codeModelFlow: Flow<String> = context.prefsDataStore.data.map { it[KEY_CODE_MODEL] ?: "auto" }
    val notificationsFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_NOTIFICATIONS] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.prefsDataStore.edit { it[KEY_DARK] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.prefsDataStore.edit { it[KEY_LANG] = lang }
    }

    suspend fun setFontScale(scale: Float) {
        val safe = scale.coerceIn(0.75f, 1.5f)
        context.prefsDataStore.edit { it[KEY_FONT_SCALE] = safe }
    }

    suspend fun setOnboardingDone() {
        context.prefsDataStore.edit { it[KEY_ONBOARDING] = true }
    }

    suspend fun setCodeModel(key: String) {
        context.prefsDataStore.edit { it[KEY_CODE_MODEL] = key }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.prefsDataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }
}
