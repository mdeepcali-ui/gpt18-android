package com.gptplus18.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.PreferencesRepository
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
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
) : ViewModel() {

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

    fun checkUpdates() {
        // TODO: force update check
    }
}
