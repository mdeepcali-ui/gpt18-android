package com.gptplus18.app.ui.screens.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.models.ServerItem
import com.gptplus18.app.data.repository.ServerRepository
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServersUiState(
    val servers: List<ServerItem> = emptyList(),
    val loading: Boolean = false,
    val error: String = "",
    val toast: String = "",
    val showAddDialog: Boolean = false,
    val testing: Int = -1,       // server id under test
    val testResult: String = "",
)

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val repo: ServerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ServersUiState())
    val state: StateFlow<ServersUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = "")
        viewModelScope.launch {
            when (val r = repo.list()) {
                is Result.Success -> _state.value = _state.value.copy(loading = false, servers = r.data)
                is Result.Error -> _state.value = _state.value.copy(loading = false, error = r.message ?: "خطأ")
            }
        }
    }

    fun showAdd() { _state.value = _state.value.copy(showAddDialog = true) }
    fun hideAdd() { _state.value = _state.value.copy(showAddDialog = false) }

    fun add(label: String, host: String, port: Int, username: String, authType: String, secret: String) {
        if (label.isBlank() || host.isBlank() || username.isBlank() || secret.isBlank()) {
            _state.value = _state.value.copy(error = "كل الحقول مطلوبة")
            return
        }
        _state.value = _state.value.copy(loading = true, error = "", showAddDialog = false)
        viewModelScope.launch {
            when (val r = repo.add(label, host, port, username, authType, secret)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(loading = false, toast = "✅ أُضيف السيرفر")
                    load()
                }
                is Result.Error -> _state.value = _state.value.copy(loading = false, error = r.message ?: "خطأ")
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            when (val r = repo.delete(id)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(toast = "🗑️ حُذف السيرفر")
                    load()
                }
                is Result.Error -> _state.value = _state.value.copy(error = r.message ?: "خطأ")
            }
        }
    }

    fun test(id: Int) {
        _state.value = _state.value.copy(testing = id, testResult = "")
        viewModelScope.launch {
            when (val r = repo.test(id)) {
                is Result.Success -> {
                    val msg = if (r.data.success) "✅ ${r.data.output.take(200)}" else "❌ ${r.data.error}"
                    _state.value = _state.value.copy(testing = -1, testResult = msg, toast = msg)
                }
                is Result.Error -> _state.value = _state.value.copy(testing = -1, toast = "❌ ${r.message}")
            }
        }
    }

    fun clearToast() { _state.value = _state.value.copy(toast = "") }
    fun clearError() { _state.value = _state.value.copy(error = "") }
}
