package com.gptplus18.app.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val darkMode: Boolean = true,
    val language: String = "ar",
    val fontScale: Float = 1.0f,
    val notificationsEnabled: Boolean = false,
    val notificationsLoading: Boolean = false,
    val notificationsError: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val notificationsRepo: NotificationsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsVM"
    }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
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
            prefs.setLanguage(if (current == "ar") "en" else "ar")
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
                // احفظ الاختيار أولاً
                prefs.setNotifications(enabled)

                if (enabled) {
                    // سجّل FCM token على السيرفر
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
                    // إلغاء التسجيل (اختياري)
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
