package com.gptplus18.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import com.gptplus18.app.data.repository.ChatRepository
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val sessions: List<Session> = emptyList(),
    val messages: List<Message> = emptyList(),
    val currentSessionId: Int? = null,
    val isSending: Boolean = false,
    val error: String? = null,
    val userName: String = "",
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = tokenStorage.getName() ?: "صديقي"
            _state.value = _state.value.copy(userName = name)
            loadSessions()
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            when (val r = chatRepo.listSessions()) {
                is Result.Success -> _state.value = _state.value.copy(sessions = r.data)
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {}
            }
        }
    }

    fun openSession(sid: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(currentSessionId = sid, messages = emptyList())
            when (val r = chatRepo.getMessages(sid)) {
                is Result.Success -> _state.value = _state.value.copy(messages = r.data)
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {}
            }
        }
    }

    fun newChat() {
        _state.value = _state.value.copy(currentSessionId = null, messages = emptyList())
    }

    fun backToList() {
        _state.value = _state.value.copy(currentSessionId = null, messages = emptyList())
        loadSessions()
    }

    fun send(text: String) {
        if (text.isBlank()) return
        val current = _state.value
        val tempMsg = Message(
            id = -1,
            role = "user",
            content = text,
            ts = System.currentTimeMillis() / 1000.0,
        )
        _state.value = current.copy(
            messages = current.messages + tempMsg,
            isSending = true,
            error = null,
        )

        viewModelScope.launch {
            when (val r = chatRepo.send(current.currentSessionId, text)) {
                is Result.Success -> {
                    val assistant = Message(
                        id = r.data.sessionId,
                        role = "assistant",
                        content = r.data.reply,
                        ts = System.currentTimeMillis() / 1000.0,
                    )
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + assistant,
                        currentSessionId = r.data.sessionId,
                        isSending = false,
                    )
                }
                is Result.Error -> _state.value = _state.value.copy(
                    isSending = false,
                    error = r.message,
                )
                else -> {}
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
