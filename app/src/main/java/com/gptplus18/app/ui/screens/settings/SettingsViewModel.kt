package com.gptplus18.app.ui.screens.settings

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class SettingsState(
    val darkMode: Boolean = true,
    val language: String = "ar",
    val fontScale: Float = 1.0f,
    val notificationsEnabled: Boolean = false,
    val notificationsLoading: Boolean = false,
    val notificationsError: String? = null,
    // ⭐ E1: بيانات المستخدم للدعم
    val userName: String = "",
    val uid: Long = 0L,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesRepository,
    private val notificationsRepo: NotificationsRepository,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsVM"
    }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        // ⭐ E1: قراءة بيانات المستخدم للدعم
        viewModelScope.launch {
            try {
                val n = tokenStorage.getName() ?: ""
                val u = tokenStorage.getUid()
                _state.value = _state.value.copy(userName = n, uid = u)
            } catch (_: Exception) { }
        }

        // 📌 طبّق اللغة المحفوظة عند الإقلاع
        viewModelScope.launch {
            val saved = prefs.langFlow.first()
            applyLocale(saved)
            _state.value = _state.value.copy(language = saved)
        }
        viewModelScope.launch {
            prefs.darkModeFlow.collect { _state.value = _state.value.copy(darkMode = it) }
        }
        viewModelScope.launch {
            prefs.langFlow.collect { _state.value = _state.value.copy(language = it) }
        }
        viewModelScope.launch {
            prefs.fontScaleFlow.collect { _state.value = _state.value.copy(fontScale = it) }
        }
        viewModelScope.launch {
            prefs.notificationsFlow.collect {
                _state.value = _state.value.copy(notificationsEnabled = it)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = prefs.langFlow.first()
            val newLang = if (current == "ar") "en" else "ar"
            prefs.setLanguage(newLang)
            _state.value = _state.value.copy(language = newLang)
            applyLocale(newLang)
        }
    }

    /**
     * يطبّق اللغة على مستوى النظام (per-app locale)
     * - API 33+: LocaleManager (يتطلب localeConfig في manifest)
     * - API < 33: Configuration override
     */
    private fun applyLocale(lang: String) {
        try {
            val tag = if (lang == "en") "en" else "ar"
            val locale = Locale.forLanguageTag(tag)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val lm = context.getSystemService(LocaleManager::class.java)
                if (lm != null) {
                    lm.applicationLocales = LocaleList.forLanguageTags(tag)
                    Log.d(TAG, "🌐 LocaleManager applied: $tag")
                } else {
                    Log.w(TAG, "⚠️ LocaleManager not available")
                }
            } else {
                Locale.setDefault(locale)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                Log.d(TAG, "🌐 Legacy locale applied: $tag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ applyLocale failed: ${e.message}", e)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { prefs.setFontScale(scale) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                notificationsEnabled = enabled,
                notificationsLoading = true,
                notificationsError = null,
            )

            try {
                prefs.setNotifications(enabled)

                if (enabled) {
                    val ok = notificationsRepo.registerCurrentToken()
                    if (!ok) {
                        _state.value = _state.value.copy(
                            notificationsEnabled = false,
                            notificationsError = "تعذر تفعيل الإشعارات — تأكد من اتصالك",
                        )
                        prefs.setNotifications(false)
                    } else {
                        Log.d(TAG, "✅ تم تفعيل الإشعارات")
                    }
                } else {
                    Log.d(TAG, "🔕 تم إلغاء الإشعارات")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    notificationsEnabled = false,
                    notificationsError = e.message ?: "خطأ",
                )
                prefs.setNotifications(false)
            } finally {
                _state.value = _state.value.copy(notificationsLoading = false)
            }
        }
    }

    fun dismissNotificationError() {
        _state.value = _state.value.copy(notificationsError = null)
    }

    fun checkUpdates() {
        // TODO: force update check
    }
}
