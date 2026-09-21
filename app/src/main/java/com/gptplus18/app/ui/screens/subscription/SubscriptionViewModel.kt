package com.gptplus18.app.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.models.Plan
import com.gptplus18.app.data.models.UserStatus
import com.gptplus18.app.data.repository.SubscriptionRepository
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionState(
    val isLoading: Boolean = true,
    val plans: Map<String, Plan> = emptyMap(),
    val wallets: Map<String, String> = emptyMap(),
    val status: UserStatus? = null,
    val selectedPlan: String? = null,
    val selectedNetwork: String? = null,
    val txHash: String = "",
    val isVerifying: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repo: SubscriptionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val r = repo.getMine()) {
                is Result.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    plans = r.data.plans,
                    wallets = r.data.wallets,
                    status = r.data.status,
                )
                is Result.Error -> {
                    // fallback — نطلب من endpoint العام
                    when (val p = repo.getPublic()) {
                        is Result.Success -> _state.value = _state.value.copy(
                            isLoading = false,
                            plans = p.data.plans,
                            wallets = p.data.wallets,
                            status = p.data.status,
                        )
                        is Result.Error -> _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = p.message,
                        )
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    fun selectPlan(planKey: String) {
        _state.value = _state.value.copy(selectedPlan = planKey, selectedNetwork = null, txHash = "")
    }

    fun selectNetwork(net: String) {
        _state.value = _state.value.copy(selectedNetwork = net, txHash = "")
    }

    fun setTxHash(v: String) {
        _state.value = _state.value.copy(txHash = v)
    }

    fun verify() {
        val plan = _state.value.selectedPlan ?: return
        val net = _state.value.selectedNetwork ?: return
        val tx = _state.value.txHash.trim()
        if (tx.length < 20) {
            _state.value = _state.value.copy(errorMessage = "رقم العملية غلط")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isVerifying = true, errorMessage = null)
            when (val r = repo.verifyPayment(plan, net, tx)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isVerifying = false,
                        successMessage = r.data.message ?: "تم تفعيل الاشتراك!",
                        txHash = "",
                    )
                    loadData()
                }
                is Result.Error -> _state.value = _state.value.copy(
                    isVerifying = false,
                    errorMessage = r.message,
                )
                else -> {}
            }
        }
    }

    fun startTrial() {
        viewModelScope.launch {
            when (val r = repo.startTrial()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(successMessage = "تم تفعيل التجربة (ساعة)")
                    loadData()
                }
                is Result.Error -> _state.value = _state.value.copy(errorMessage = r.message)
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}
