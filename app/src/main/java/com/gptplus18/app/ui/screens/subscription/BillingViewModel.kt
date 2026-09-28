package com.gptplus18.app.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.models.*
import com.gptplus18.app.data.repository.BillingRepository
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillingUiState(
    val isLoading: Boolean = true,
    val plans: List<BillingPlan> = emptyList(),
    val me: BillingMeResponse? = null,
    val transactions: List<BillingTransaction> = emptyList(),
    val selectedPlan: String? = null,
    val selectedNetwork: String = "bep20",
    val txHash: String = "",
    val isVerifying: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repo: BillingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // 1) الباقات (بدون توكن)
            when (val p = repo.getPlans()) {
                is Result.Success -> _state.value = _state.value.copy(plans = p.data)
                is Result.Error -> _state.value = _state.value.copy(errorMessage = p.message)
                else -> {}
            }

            // 2) اشتراكي (يحتاج توكن)
            when (val m = repo.getMyBilling()) {
                is Result.Success -> _state.value = _state.value.copy(me = m.data)
                is Result.Error -> { /* قد يكون غير مسجل دخول */ }
                else -> {}
            }

            // 3) سجل الدفعات
            when (val t = repo.getTransactions()) {
                is Result.Success -> _state.value = _state.value.copy(transactions = t.data)
                is Result.Error -> { /* اختياري */ }
                else -> {}
            }

            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun selectPlan(key: String) {
        _state.value = _state.value.copy(
            selectedPlan = key,
            txHash = "",
            errorMessage = null,
            successMessage = null,
        )
    }

    fun selectNetwork(net: String) {
        _state.value = _state.value.copy(selectedNetwork = net, txHash = "")
    }

    fun setTxHash(v: String) {
        _state.value = _state.value.copy(txHash = v)
    }

    fun verifyPayment() {
        val plan = _state.value.selectedPlan ?: return
        val net = _state.value.selectedNetwork
        val tx = _state.value.txHash.trim()

        if (tx.length < 20) {
            _state.value = _state.value.copy(errorMessage = "رقم العملية غير صالح")
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
                        selectedPlan = null,
                    )
                    loadAll()
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isVerifying = false,
                        errorMessage = r.message,
                    )
                }
                else -> {
                    _state.value = _state.value.copy(isVerifying = false)
                }
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}
