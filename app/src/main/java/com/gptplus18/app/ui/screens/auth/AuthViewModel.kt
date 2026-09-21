package com.gptplus18.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.repository.AuthRepository
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
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
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
                is Result.Success -> _state.value = AuthUiState(isAuthenticated = true)
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
                is Result.Success -> _state.value = AuthUiState(isAuthenticated = true)
                is Result.Error -> _state.value = AuthUiState(error = r.message)
                else -> {}
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
