package com.gptplus18.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.models.AdminUser
import com.gptplus18.app.data.repository.AdminRepository
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AdminFilter { ALL, SUBSCRIBED, TRIAL, NONE }

data class AdminStats(
    val total: Int = 0,
    val subscribed: Int = 0,
    val trial: Int = 0,
    val free: Int = 0,
)

data class AdminUiState(
    val isOwner: Boolean = false,
    val isChecking: Boolean = true,
    val isLoading: Boolean = false,
    val users: List<AdminUser> = emptyList(),
    val filtered: List<AdminUser> = emptyList(),
    val search: String = "",
    val filter: AdminFilter = AdminFilter.ALL,
    val stats: AdminStats = AdminStats(),
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repo: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init { checkOwner() }

    private fun checkOwner() {
        viewModelScope.launch {
            val owner = repo.isOwner()
            _state.value = _state.value.copy(isOwner = owner, isChecking = false)
            if (owner) loadUsers()
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val r = repo.listUsers()) {
                is Result.Success -> {
                    val users = r.data
                    val stats = computeStats(users)
                    _state.value = _state.value.copy(
                        users = users,
                        filtered = applyFilter(users, _state.value.search, _state.value.filter),
                        stats = stats,
                        isLoading = false,
                    )
                }
                is Result.Error -> _state.value = _state.value.copy(
                    error = r.message,
                    isLoading = false,
                )
                else -> {}
            }
        }
    }

    private fun computeStats(users: List<AdminUser>): AdminStats {
        val now = System.currentTimeMillis() / 1000.0
        var sub = 0; var tr = 0
        users.forEach { u ->
            when {
                (u.subExpires) > now -> sub++
                (u.trialExpires) > now -> tr++
            }
        }
        return AdminStats(
            total = users.size,
            subscribed = sub,
            trial = tr,
            free = users.size - sub - tr,
        )
    }

    private fun applyFilter(users: List<AdminUser>, q: String, f: AdminFilter): List<AdminUser> {
        val now = System.currentTimeMillis() / 1000.0
        var list = users
        if (q.isNotBlank()) {
            list = list.filter {
                it.email.contains(q, ignoreCase = true) ||
                it.name.contains(q, ignoreCase = true)
            }
        }
        list = when (f) {
            AdminFilter.ALL -> list
            AdminFilter.SUBSCRIBED -> list.filter { it.subExpires > now }
            AdminFilter.TRIAL -> list.filter { it.trialExpires > now && it.subExpires <= now }
            AdminFilter.NONE -> list.filter { it.subExpires <= now && it.trialExpires <= now }
        }
        return list
    }

    fun setSearch(q: String) {
        _state.value = _state.value.copy(
            search = q,
            filtered = applyFilter(_state.value.users, q, _state.value.filter),
        )
    }

    fun setFilter(f: AdminFilter) {
        _state.value = _state.value.copy(
            filter = f,
            filtered = applyFilter(_state.value.users, _state.value.search, f),
        )
    }

    fun grant(email: String, plan: String) {
        if (email.isBlank()) {
            _state.value = _state.value.copy(error = "اكتب الإيميل")
            return
        }
        viewModelScope.launch {
            when (val r = repo.grant(email, plan)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(message = r.data)
                    loadUsers()
                }
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {}
            }
        }
    }

    fun revoke(email: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            when (val r = repo.revoke(email)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(message = r.data)
                    loadUsers()
                }
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {}
            }
        }
    }

    fun resetTrial(email: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            when (val r = repo.resetTrial(email)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(message = r.data)
                    loadUsers()
                }
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}
