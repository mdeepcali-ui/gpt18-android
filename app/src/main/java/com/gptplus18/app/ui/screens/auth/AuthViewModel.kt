package com.gptplus18.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.repository.AuthRepository
import com.gptplus18.app.util.AnalyticsHelper
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val pendingGoogleToken: Pair<String, String>? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "املأ كل الحقول")
            return
        }
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            when (val r = repo.login(email, password)) {
                is Result.Success -> {
                    // 📊 Analytics
                    analytics.logLogin(method = "email")
                    _state.value = AuthUiState(isAuthenticated = true)
                }
                is Result.Error -> _state.value = AuthUiState(error = r.message)
                else -> {}
            }
        }
    }

    fun signup(name: String, email: String, password: String) {
        if (name.length < 2) { _state.value = _state.value.copy(error = "الاسم قصير"); return }
        if (email.isBlank()) { _state.value = _state.value.copy(error = "اكتب الإيميل"); return }
        if (password.length < 6) { _state.value = _state.value.copy(error = "كلمة السر 6 أحرف على الأقل"); return }

        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            when (val r = repo.signup(name, email, password)) {
                is Result.Success -> {
                    // 📊 Analytics
                    analytics.logSignup(method = "email")
                    _state.value = AuthUiState(isAuthenticated = true)
                }
                is Result.Error -> _state.value = AuthUiState(error = r.message)
                else -> {}
            }
        }
    }

    fun saveGoogleSession(token: String, name: String) {
        viewModelScope.launch {
            tokenStorage.save(token, name, "", 0)
            // 📊 Analytics
            analytics.logLogin(method = "google")
            _state.value = _state.value.copy(pendingGoogleToken = Pair(token, name))
        }
    }

    fun consumeGoogleToken() {
        _state.value = _state.value.copy(pendingGoogleToken = null)
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
