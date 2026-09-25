package com.gptplus18.app.ui.screens.code

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.models.CodeFile
import com.gptplus18.app.data.models.CodeJobStatus
import com.gptplus18.app.data.models.CodeLogEntry
import com.gptplus18.app.data.models.CodeMessage
import com.gptplus18.app.data.models.CodeModel
import com.gptplus18.app.data.models.CodeSession
import com.gptplus18.app.data.api.StreamEvent
import com.gptplus18.app.data.repository.CodeRepository
import com.gptplus18.app.util.AnalyticsHelper
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    // ⭐ الملفات الجاهزة للتحميل من آخر مهمة
    val files: List<CodeFile> = emptyList(),
    val zipUrl: String? = null,
    val statusLabel: String = "",
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
        loadSessions()
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
            currentSessionId = -1,
            messages = emptyList(),
            liveOutput = "",
            logs = emptyList(),
            error = null,
            jobStatus = "idle",
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
        val sidForApi = if (sid == -1) null else sid
        val model = _state.value.currentModel

        // ⭐ رسائل مؤقتة فوراً (زي Chat)
        val now = System.currentTimeMillis() / 1000.0
        val tempUser = CodeMessage(id = -1, role = "user", content = request, ts = now)
        val tempAssistant = CodeMessage(id = -2, role = "assistant", content = "", ts = now + 1)

        _state.value = _state.value.copy(
            messages = _state.value.messages + tempUser + tempAssistant,
            isRunning = true,
            error = null,
            logs = emptyList(),
            liveOutput = "",
            jobStatus = "starting",
            statusLabel = "🎙️ يحلل الطلب...",
        )

        viewModelScope.launch {
            val modelKey = model.key
            val sb = StringBuilder()
            var finalSid = sidForApi

            try {
                analytics.logCodeJobStarted(model = modelKey)
                repo.streamCode(sidForApi, request, modelKey).collect { ev ->
                    when (ev) {
                        is StreamEvent.Status -> {
                            _state.value = _state.value.copy(statusLabel = ev.text)
                        }
                        is StreamEvent.Delta -> {
                            sb.append(ev.text)
                            val currentText = sb.toString()
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == tempAssistant.ts) {
                                        m.copy(content = currentText)
                                    } else m
                                },
                                statusLabel = "✍️ يكتب...",
                            )
                        }
                        is StreamEvent.Done -> {
                            if (ev.sessionId > 0) finalSid = ev.sessionId
                            val cleanedFinal = sb.toString().trim()
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == tempAssistant.ts) {
                                        m.copy(id = finalSid ?: 0, content = cleanedFinal)
                                    } else m
                                },
                                currentSessionId = finalSid,
                                isRunning = false,
                                jobStatus = "done",
                                statusLabel = "",
                            )
                            loadSessions()
                        }
                        is StreamEvent.Error -> {
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.filterNot {
                                    it.id == -2 && it.ts == tempAssistant.ts
                                },
                                isRunning = false,
                                jobStatus = "error",
                                error = ev.message,
                                statusLabel = "",
                            )
                        }
                        else -> {}
                    }
                }

                if (sb.isEmpty()) {
                    _state.value = _state.value.copy(
                        messages = _state.value.messages.filterNot {
                            it.id == -2 && it.ts == tempAssistant.ts
                        },
                        isRunning = false,
                        error = "لم يصل رد",
                        statusLabel = "",
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    messages = _state.value.messages.filterNot {
                        it.id == -2 && it.ts == tempAssistant.ts
                    },
                    isRunning = false,
                    statusLabel = "",
                    error = e.message ?: "انقطع الاتصال",
                )
            } finally {
                if (_state.value.isRunning) {
                    _state.value = _state.value.copy(
                        isRunning = false,
                        statusLabel = "",
                    )
                }
                loadSessions()
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
        // ⭐ استخراج الملفات من نتيجة الـ job
        val res = js.result
        val filesList = res?.files ?: emptyList()
        val zip = res?.zipUrl
        _state.value = _state.value.copy(
            isRunning = false,
            jobStatus = "done",
            files = filesList,
            zipUrl = zip,
        )
        if (sid != null && sid > 0) {
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
