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
    private val KEY_DISCLAIMER = booleanPreferencesKey("disclaimer_accepted")
    private val KEY_AGE_VERIFIED = booleanPreferencesKey("age_verified")

    val darkModeFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_DARK] ?: true }
    val langFlow: Flow<String> = context.prefsDataStore.data.map { it[KEY_LANG] ?: "ar" }
    val fontScaleFlow: Flow<Float> = context.prefsDataStore.data.map { it[KEY_FONT_SCALE] ?: 1.0f }
    val onboardingDoneFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_ONBOARDING] ?: false }
    val notificationsFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_NOTIFICATIONS] ?: false }
    val disclaimerAcceptedFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_DISCLAIMER] ?: false }
    val ageVerifiedFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_AGE_VERIFIED] ?: false }

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

    suspend fun setNotifications(enabled: Boolean) {
        context.prefsDataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    suspend fun setDisclaimerAccepted() {
        context.prefsDataStore.edit { it[KEY_DISCLAIMER] = true }
    }

    suspend fun setAgeVerified() {
        context.prefsDataStore.edit { it[KEY_AGE_VERIFIED] = true }
    }
}
