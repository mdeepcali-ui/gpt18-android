package com.gptplus18.app.ui.screens.code

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.models.CodeJobStatus
import com.gptplus18.app.data.models.CodeLogEntry
import com.gptplus18.app.data.models.CodeMessage
import com.gptplus18.app.data.models.CodeModel
import com.gptplus18.app.data.models.CodeSession
import com.gptplus18.app.data.repository.CodeRepository
import com.gptplus18.app.util.AnalyticsHelper
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CodeUiState(
    val sessions: List<CodeSession> = emptyList(),
    val messages: List<CodeMessage> = emptyList(),
    val currentSessionId: Int? = null,
    val isRunning: Boolean = false,
    val logs: List<CodeLogEntry> = emptyList(),
    val liveOutput: String = "",
    val error: String? = null,
    val jobStatus: String = "idle",
    val currentModel: CodeModel = CodeModel.AUTO,
)

@HiltViewModel
class CodeViewModel @Inject constructor(
    private val repo: CodeRepository,
    private val prefs: PreferencesRepository,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(CodeUiState())
    val state: StateFlow<CodeUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    init {
        viewModelScope.launch {
            val saved = prefs.codeModelFlow.first()
            val model = CodeModel.fromKey(saved)
            _state.value = _state.value.copy(currentModel = model)
        }
        loadSessions()
    }

    fun setModel(m: CodeModel) {
        _state.value = _state.value.copy(currentModel = m)
        viewModelScope.launch {
            prefs.setCodeModel(m.key)
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            when (val r = repo.listSessions()) {
                is Result.Success -> _state.value = _state.value.copy(sessions = r.data)
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {}
            }
        }
    }

    fun openSession(sid: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentSessionId = sid,
                messages = emptyList(),
                liveOutput = "",
            )
            when (val r = repo.getMessages(sid)) {
                is Result.Success -> _state.value = _state.value.copy(messages = r.data)
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {}
            }
        }
    }

    fun newRequest() {
        _state.value = _state.value.copy(
            currentSessionId = null,
            messages = emptyList(),
            liveOutput = "",
            logs = emptyList(),
        )
    }

    fun backToList() {
        stopPolling()
        _state.value = _state.value.copy(
            currentSessionId = null,
            messages = emptyList(),
            isRunning = false,
            liveOutput = "",
            logs = emptyList(),
        )
        loadSessions()
    }

    fun deleteSession(sid: Int) {
        viewModelScope.launch {
            when (repo.deleteSession(sid)) {
                is Result.Success -> loadSessions()
                is Result.Error -> _state.value = _state.value.copy(error = "فشل الحذف")
                else -> {}
            }
        }
    }

    fun submit(request: String) {
        if (request.isBlank()) return
        if (_state.value.isRunning) return

        val sid = _state.value.currentSessionId
        val model = _state.value.currentModel
        _state.value = _state.value.copy(
            isRunning = true,
            error = null,
            logs = emptyList(),
            liveOutput = "",
            jobStatus = "starting",
        )

        viewModelScope.launch {
            val modelKey = model.key
            when (val r = repo.generate(request, sid, modelKey)) {
                is Result.Success -> {
                    // 📊 Analytics
                    analytics.logCodeJobStarted(model = modelKey)
                    _state.value = _state.value.copy(
                        currentSessionId = r.data.sessionId,
                        jobStatus = "running",
                    )
                    startPolling(r.data.jobId)
                }
                is Result.Error -> _state.value = _state.value.copy(
                    isRunning = false,
                    jobStatus = "error",
                    error = r.message,
                )
                else -> {}
            }
        }
    }

    private fun startPolling(jobId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            var attempts = 0
            while (attempts < 300) {
                delay(2000)
                when (val r = repo.getJobStatus(jobId)) {
                    is Result.Success -> {
                        val js: CodeJobStatus = r.data
                        _state.value = _state.value.copy(
                            logs = js.log,
                            jobStatus = js.status,
                        )
                        if (js.status == "done") {
                            onJobDone(js)
                            return@launch
                        }
                        if (js.status == "error") {
                            _state.value = _state.value.copy(
                                isRunning = false,
                                jobStatus = "error",
                                error = js.error ?: "فشل التنفيذ",
                            )
                            return@launch
                        }
                    }
                    is Result.Error -> { }
                    else -> {}
                }
                attempts++
            }
            _state.value = _state.value.copy(
                isRunning = false,
                jobStatus = "error",
                error = "انتهت المهلة",
            )
        }
    }

    private fun onJobDone(js: CodeJobStatus) {
        val sid = _state.value.currentSessionId
        _state.value = _state.value.copy(
            isRunning = false,
            jobStatus = "done",
        )
        if (sid != null) {
            viewModelScope.launch {
                when (val r = repo.getMessages(sid)) {
                    is Result.Success -> _state.value = _state.value.copy(messages = r.data)
                    else -> {}
                }
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }
}
