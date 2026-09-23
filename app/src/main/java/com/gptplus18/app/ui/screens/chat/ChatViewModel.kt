package com.gptplus18.app.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.api.StreamEvent
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.Attachment
import com.gptplus18.app.data.models.ChatMode
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import com.gptplus18.app.data.models.ThinkingData
import com.gptplus18.app.data.repository.CacheRepository
import com.gptplus18.app.data.repository.AdminRepository
import com.gptplus18.app.data.repository.ChatRepository
import com.gptplus18.app.data.repository.UploadRepository
import com.gptplus18.app.util.AnalyticsHelper
import com.gptplus18.app.util.RateLimiter
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val sessions: List<Session> = emptyList(),
    val allSessions: List<AllSession> = emptyList(),
    val filteredSessions: List<Session> = emptyList(),
    val messages: List<Message> = emptyList(),
    val currentSessionId: Int? = null,
    val isSending: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val userName: String = "",
    val statusLabel: String = "يفكر",
    val searchQuery: String = "",
    val replyTo: Message? = null,
    val isOnline: Boolean = true,
    val currentMode: ChatMode = ChatMode.CHAT,
    val isSubscribed: Boolean = false,
    val thinkingByMessage: Map<Long, ThinkingData> = emptyMap(),
    val pendingAttachments: List<Attachment> = emptyList(),
    val isOwner: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val uploadRepo: UploadRepository,
    private val cacheRepo: CacheRepository,
    private val tokenStorage: TokenStorage,
    private val analytics: AnalyticsHelper,
    private val adminRepo: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = tokenStorage.getName()?.takeIf { it.isNotBlank() } ?: "المستخدم"
            _state.value = _state.value.copy(userName = name)
            loadAllSessions()
        }
        viewModelScope.launch {
            // 🔁 محاولة 3 مرات مع تأخير
            var owner = false
            repeat(3) { attempt ->
                try {
                    owner = adminRepo.isOwner()
                    if (owner) return@repeat
                } catch (_: Exception) { }
                if (attempt < 2) kotlinx.coroutines.delay(1000L * (attempt + 1))
            }
            _state.value = _state.value.copy(isOwner = owner)
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            val cached = cacheRepo.getSessions()
            if (cached.isNotEmpty()) {
                _state.value = _state.value.copy(
                    sessions = cached,
                    filteredSessions = filterSessions(cached, _state.value.searchQuery),
                )
            }

    // 📋 تحميل كل الجلسات (Chat + Code) موحّدة
    fun loadAllSessions() {
        viewModelScope.launch {
            when (val r = chatRepo.listAllSessions()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(allSessions = r.data)
                }
                else -> { /* ما نعمل شي */ }
            }
        }
    }
            when (val r = chatRepo.listSessions()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        sessions = r.data,
                        filteredSessions = filterSessions(r.data, _state.value.searchQuery),
                        isOnline = true,
                    )
                    cacheRepo.saveSessions(r.data)
                }
                is Result.Error -> {
                    if (cached.isEmpty()) {
                        _state.value = _state.value.copy(
                            error = r.message,
                            isOnline = false,
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private fun filterSessions(list: List<Session>, query: String): List<Session> {
        if (query.isBlank()) return list
        return list.filter {
            it.title.contains(query, ignoreCase = true) ||
                (it.lastMsg?.contains(query, ignoreCase = true) == true)
        }
    }

    fun setSearch(q: String) {
        _state.value = _state.value.copy(
            searchQuery = q,
            filteredSessions = filterSessions(_state.value.sessions, q),
        )
    }

    fun openSession(sid: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentSessionId = sid,
                messages = emptyList(),
                replyTo = null,
            )
            val cached = cacheRepo.getMessages(sid)
            if (cached.isNotEmpty()) {
                _state.value = _state.value.copy(messages = cached)
            }
            when (val r = chatRepo.getMessages(sid)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(messages = r.data)
                    cacheRepo.saveMessages(sid, r.data)
                }
                is Result.Error -> {
                    if (cached.isEmpty()) {
                        _state.value = _state.value.copy(error = r.message)
                    }
                }
                else -> {}
            }
        }
    }

    fun newChat() {
        // 📊 Analytics
        analytics.logChatStart()
        // 🆕 -1 = شات جديدة غير محفوظة (يظهر شاشة دردشة فاضية)
        _state.value = _state.value.copy(
            currentSessionId = -1,
            messages = emptyList(),
            replyTo = null,
            pendingAttachments = emptyList(),
        )
    }

    fun backToList() {
        _state.value = _state.value.copy(
            currentSessionId = null,
            messages = emptyList(),
            replyTo = null,
        )
        loadSessions()
    }

    fun deleteSession(sid: Int) {
        viewModelScope.launch {
            cacheRepo.deleteSession(sid)
            when (chatRepo.deleteSession(sid)) {
                is Result.Success -> loadSessions()
                is Result.Error -> _state.value = _state.value.copy(error = "فشل الحذف")
                else -> {}
            }
        }
    }

    fun deleteMessage(msgId: Int) {
        _state.value = _state.value.copy(
            messages = _state.value.messages.filter { it.id != msgId || it.id < 0 },
        )
    }

    fun setReplyTo(msg: Message?) {
        _state.value = _state.value.copy(replyTo = msg)
    }

    fun send(text: String) {
        if (text.isBlank()) return

        RateLimiter.checkChat()?.let { err ->
            _state.value = _state.value.copy(error = err)
            return
        }

        // 📊 Analytics
        analytics.logMessageSent(length = text.length)

        val current = _state.value
        val replyPrefix = current.replyTo?.let { "↩︎ ${it.content.take(40)}\n" } ?: ""
        val finalText = replyPrefix + text

        val tempMsg = Message(
            id = -1,
            role = "user",
            content = finalText,
            ts = System.currentTimeMillis() / 1000.0,
        )
        val thinkId = System.currentTimeMillis()
        val thinkingSteps = when (current.currentMode.key) {
            "code" -> listOf("تحليل الطلب", "بناء الخطة", "كتابة الكود", "المراجعة")
            "media" -> listOf("فهم الوصف", "توليد النموذج", "التحسين النهائي")
            "max" -> listOf("التحليل", "التخطيط", "التنفيذ", "الصياغة")
            else -> listOf("فهم السؤال", "جمع السياق", "صياغة الرد")
        }

        val assistantTs = System.currentTimeMillis() / 1000.0 + 1
        val emptyAssistant = Message(
            id = -2,
            role = "assistant",
            content = "",
            ts = assistantTs,
        )

        _state.value = current.copy(
            messages = current.messages + tempMsg + emptyAssistant,
            isSending = true,
            error = null,
            statusLabel = "يفكر",
            replyTo = null,
            thinkingByMessage = current.thinkingByMessage + (thinkId to ThinkingData(
                steps = thinkingSteps,
                status = "think",
            )),
        )

        viewModelScope.launch {
            val sb = StringBuilder()
            var finalSessionId = current.currentSessionId
            var gotError = false

            // 🆕 -1 = شات جديدة → نمرر null للسيرفر (ينشئ session جديدة)
            val sidForServer = if (current.currentSessionId == -1) null else current.currentSessionId
            chatRepo.streamMessage(sidForServer, finalText)
                .collect { ev ->
                    when (ev) {
                        is StreamEvent.Delta -> {
                            sb.append(ev.text)
                            val cleaned = cleanStreamingText(sb.toString())
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == assistantTs) {
                                        m.copy(content = cleaned)
                                    } else m
                                },
                                statusLabel = "يكتب",
                            )
                        }
                        is StreamEvent.Done -> {
                            finalSessionId = if (ev.sessionId > 0) ev.sessionId else current.currentSessionId
                            val newThinking = if (ev.thinking.isNotBlank()) {
                                _state.value.thinkingByMessage + (thinkId to ThinkingData(
                                    steps = thinkingSteps,
                                    status = "think",
                                    rawText = ev.thinking,
                                ))
                            } else {
                                _state.value.thinkingByMessage
                            }
                            val cleanedFinal = cleanStreamingText(sb.toString())
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == assistantTs) {
                                        m.copy(id = finalSessionId ?: 0, content = cleanedFinal)
                                    } else m
                                },
                                currentSessionId = finalSessionId,
                                isSending = false,
                                statusLabel = "يفكر",
                                thinkingByMessage = newThinking,
                            )
                            finalSessionId?.let { sid ->
                                cacheRepo.saveMessages(sid, _state.value.messages)
                            }
                        }
                        is StreamEvent.Error -> {
                            gotError = true
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.filterNot { it.id == -2 && it.ts == assistantTs },
                                isSending = false,
                                error = ev.message,
                            )
                        }
                    }
                }

            if (!gotError && sb.isEmpty()) {
                _state.value = _state.value.copy(
                    messages = _state.value.messages.filterNot { it.id == -2 && it.ts == assistantTs },
                    isSending = false,
                    error = "لم يصل رد من السيرفر",
                )
            }
        }
    }

    /**
     * 🎨 تنقية نص الستريم من وسوم التفكير
     * - لو النموذج لسا ما وصل [[رد]] → يرجع فاضي
     * - لو في [[رد]]...[[/رد]] → يرجع النص بينهم فقط
     */
    private fun cleanStreamingText(raw: String): String {
        val replyStart = raw.indexOf("[[رد]]")
        if (replyStart == -1) return ""

        val afterReply = raw.substring(replyStart + "[[رد]]".length)
        val replyEnd = afterReply.indexOf("[[/رد]]")

        return if (replyEnd == -1) {
            afterReply.trimStart()
        } else {
            afterReply.substring(0, replyEnd).trimStart()
        }
    }

    fun uploadAndSend(uri: Uri, mimeType: String, fileName: String, caption: String) {
        val current = _state.value
        val displayName = if (mimeType.startsWith("image/")) "[صورة]" else "[$fileName]"
        val tempMsg = Message(
            id = -2,
            role = "user",
            content = if (caption.isBlank()) displayName else "$displayName\n$caption",
            ts = System.currentTimeMillis() / 1000.0,
        )
        _state.value = current.copy(
            messages = current.messages + tempMsg,
            isUploading = true,
            error = null,
            statusLabel = if (mimeType.startsWith("image/")) "يحلل" else "يفكر",
        )

        viewModelScope.launch {
            when (val up = uploadRepo.uploadFile(uri, mimeType, fileName)) {
                is Result.Success -> {
                    val fileId = up.data.fileId ?: return@launch
                    when (val r = uploadRepo.processUploaded(fileId, caption, current.currentSessionId)) {
                        is Result.Success -> {
                            val reply = r.data.reply ?: "تم"
                            val assistant = Message(
                                id = (r.data.sessionId ?: 0),
                                role = "assistant",
                                content = reply,
                                ts = System.currentTimeMillis() / 1000.0,
                            )
                            _state.value = _state.value.copy(
                                messages = _state.value.messages + assistant,
                                currentSessionId = r.data.sessionId ?: current.currentSessionId,
                                isUploading = false,
                            )
                        }
                        is Result.Error -> _state.value = _state.value.copy(
                            isUploading = false,
                            error = r.message,
                        )
                        else -> {}
                    }
                }
                is Result.Error -> _state.value = _state.value.copy(
                    isUploading = false,
                    error = up.message,
                )
                else -> {}
            }
        }
    }

    fun setMode(mode: ChatMode) {
        _state.value = _state.value.copy(currentMode = mode)
    }

    fun uploadMultiple(uris: List<Uri>, mimeTypes: List<String>, fileNames: List<String>, caption: String) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            uris.forEachIndexed { index, uri ->
                val mime = mimeTypes.getOrNull(index) ?: "application/octet-stream"
                val name = fileNames.getOrNull(index) ?: "file_$index"
                uploadAndSend(uri, mime, name, if (index == 0) caption else "")
            }
        }
    }

    fun addAttachment(uri: Uri, mimeType: String, fileName: String, sizeBytes: Long) {
        val id = System.currentTimeMillis()
        val att = Attachment(
            id = id,
            uri = uri,
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            progress = 0.01f,
        )
        val newLabel = when {
            mimeType.startsWith("image/") -> "يحلل"
            mimeType.startsWith("video/") -> "يحلل"
            else -> "يفكر"
        }
        _state.value = _state.value.copy(
            pendingAttachments = _state.value.pendingAttachments + att,
            statusLabel = newLabel,
        )
        viewModelScope.launch {
            updateAttachment(id) { it.copy(progress = 0.3f) }
            when (val up = uploadRepo.uploadFile(uri, mimeType, fileName)) {
                is Result.Success -> {
                    updateAttachment(id) {
                        it.copy(progress = 1f, uploadedFileId = up.data.fileId)
                    }
                }
                is Result.Error -> {
                    updateAttachment(id) { it.copy(error = up.message) }
                }
                else -> {}
            }
        }
    }

    fun removeAttachment(id: Long) {
        _state.value = _state.value.copy(
            pendingAttachments = _state.value.pendingAttachments.filter { it.id != id },
        )
    }

    private fun updateAttachment(id: Long, block: (Attachment) -> Attachment) {
        _state.value = _state.value.copy(
            pendingAttachments = _state.value.pendingAttachments.map {
                if (it.id == id) block(it) else it
            },
        )
    }

    fun sendWithAttachments(caption: String) {
        RateLimiter.checkChat()?.let { err ->
            _state.value = _state.value.copy(error = err)
            return
        }

        val atts = _state.value.pendingAttachments
        if (atts.isEmpty()) {
            if (caption.isNotBlank()) send(caption)
            return
        }
        if (atts.any { !it.isUploaded }) {
            _state.value = _state.value.copy(error = "انتظر انتهاء الرفع")
            return
        }

        if (caption.isNotBlank()) {
            send(caption)
        }

        val sessionId = _state.value.currentSessionId
        viewModelScope.launch {
            for (att in atts) {
                val fid = att.uploadedFileId ?: continue
                when (val r = uploadRepo.processUploaded(fid, "", sessionId)) {
                    is Result.Success -> {
                        val reply = r.data.reply ?: "تم"
                        val assistant = Message(
                            id = (r.data.sessionId ?: 0),
                            role = "assistant",
                            content = reply,
                            ts = System.currentTimeMillis() / 1000.0,
                        )
                        _state.value = _state.value.copy(
                            messages = _state.value.messages + assistant,
                            currentSessionId = r.data.sessionId ?: _state.value.currentSessionId,
                        )
                    }
                    else -> {}
                }
            }
            _state.value = _state.value.copy(pendingAttachments = emptyList())
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
