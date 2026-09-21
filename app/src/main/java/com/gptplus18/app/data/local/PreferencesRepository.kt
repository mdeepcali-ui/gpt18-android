package com.gptplus18.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    val darkModeFlow: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_DARK] ?: true }
    val langFlow: Flow<String> = context.prefsDataStore.data.map { it[KEY_LANG] ?: "ar" }

    suspend fun setDarkMode(enabled: Boolean) {
        context.prefsDataStore.edit { it[KEY_DARK] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.prefsDataStore.edit { it[KEY_LANG] = lang }
    }
}
