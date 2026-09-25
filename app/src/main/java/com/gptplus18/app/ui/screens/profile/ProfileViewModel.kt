package com.gptplus18.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.repository.AdminRepository
import com.gptplus18.app.data.repository.AuthRepository
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val userName: String = "",
    val userEmail: String = "",
    val avatarUrl: String = "",
    val isUploadingAvatar: Boolean = false,
    val darkMode: Boolean = true,
    val language: String = "ar",
    val hasSubscription: Boolean = false,
    val hasTrial: Boolean = false,
    val isOwner: Boolean = false,
    val fontScale: Float = 1.0f,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val prefs: PreferencesRepository,
    private val authRepo: AuthRepository,
    private val adminRepo: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = tokenStorage.getName() ?: ""
            val email = tokenStorage.getEmail() ?: ""
            _state.value = _state.value.copy(userName = name, userEmail = email)
        }
        viewModelScope.launch {
            prefs.darkModeFlow.collect { dark ->
                _state.value = _state.value.copy(darkMode = dark)
            }
        }
        viewModelScope.launch {
            prefs.langFlow.collect { lang ->
                _state.value = _state.value.copy(language = lang)
            }
        }
        viewModelScope.launch {
            prefs.fontScaleFlow.collect { scale ->
                _state.value = _state.value.copy(fontScale = scale)
            }
        }
        viewModelScope.launch {
            when (val r = authRepo.me()) {
                is Result.Success -> _state.value = _state.value.copy(
                    userName = r.data.name,
                    userEmail = r.data.email,
                    avatarUrl = r.data.avatarUrl ?: "",
                    hasSubscription = r.data.hasSubscription,
                    hasTrial = r.data.hasActiveTrial,
                )
                else -> {}
            }
        }
        viewModelScope.launch {
            val owner = adminRepo.isOwner()
            _state.value = _state.value.copy(isOwner = owner)
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
        viewModelScope.launch {
            prefs.setFontScale(scale)
        }
    }

    fun logout() {
        viewModelScope.launch { authRepo.logout() }
    }

    fun setAvatar(url: String) {
        _state.value = _state.value.copy(avatarUrl = url, isUploadingAvatar = true)
        viewModelScope.launch {
            try {
                val r = authRepo.updateProfile(avatarUrl = url)
                when (r) {
                    is Result.Success -> {
                        val finalUrl = r.data.avatarUrl ?: url
                        // ⭐ نحفظ في TokenStorage حتى يظهر في Drawer
                        try { tokenStorage.setAvatar(finalUrl) } catch (_: Exception) {}
                        _state.value = _state.value.copy(
                            avatarUrl = finalUrl,
                            isUploadingAvatar = false,
                        )
                    }
                    else -> {
                        _state.value = _state.value.copy(isUploadingAvatar = false)
                    }
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(isUploadingAvatar = false)
            }
        }
    }
}
