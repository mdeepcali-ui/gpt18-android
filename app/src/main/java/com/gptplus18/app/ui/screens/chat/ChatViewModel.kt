package com.gptplus18.app.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.api.StreamEvent
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.AllSession
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
    val avatarUrl: String = "",
    val statusLabel: String = "يفكر",
    val searchQuery: String = "",
    val replyTo: Message? = null,
    val isOnline: Boolean = true,
    val currentMode: ChatMode = ChatMode.CHAT,
    val pendingMediaType: String? = null,  // ⭐ v2.0: "song" / "video" / "image" / null
    val codeFiles: List<com.gptplus18.app.data.api.CodeFileItem> = emptyList(),
    val codeZipUrl: String? = null,
    val isSubscribed: Boolean = false,
    val thinkingByMessage: Map<Long, ThinkingData> = emptyMap(),
    val pendingAttachments: List<Attachment> = emptyList(),
    val isOwner: Boolean = false,
    val pinnedMessages: List<Message> = emptyList(),
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
            val avatar = tokenStorage.getAvatar() ?: ""
            _state.value = _state.value.copy(userName = name, avatarUrl = avatar)
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
        loadAllSessions()
    }

    fun deleteSession(sid: Int) {
        viewModelScope.launch {
            cacheRepo.deleteSession(sid)
            when (chatRepo.deleteSession(sid)) {
                is Result.Success -> {
                    loadSessions()
                    loadAllSessions()
                }
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
            codeFiles = emptyList(),
            codeZipUrl = null,
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
            val thinkingSb = StringBuilder()

            var gotImage = false
            try {
            chatRepo.streamMessage(sidForServer, finalText)
                .collect { ev ->
                    when (ev) {
                        // ⭐ التفكير — يُجمع في السحابة (لا يظهر في الرسالة)
                        is StreamEvent.Status -> {
                            // ⭐ v2.0: كشف نوع الوسائط الجاري إنشاؤها
                            val _t = ev.text
                            val _pmedia = when {
                                _t.contains("أغنية") || _t.contains("تلحين") || _t.contains("🎵") -> "song"
                                _t.contains("فيديو") || _t.contains("🎬") -> "video"
                                _t.contains("صورة") || _t.contains("🎨") -> "image"
                                else -> _state.value.pendingMediaType
                            }
                            _state.value = _state.value.copy(
                                statusLabel = ev.text,
                                pendingMediaType = _pmedia,
                            )
                        }
                        is StreamEvent.ImageUrl -> {
                            gotImage = true
                            val md = "\n\n![صورة](${ev.url})\n"
                            sb.append(md)
                            val currentText = sb.toString()
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == assistantTs) {
                                        m.copy(content = currentText)
                                    } else m
                                },
                                statusLabel = "🎨 الصورة جاهزة",
                            )
                        }
                        is StreamEvent.ThinkingDelta -> {
                            thinkingSb.append(ev.text)
                            val current = _state.value.thinkingByMessage[thinkId]
                            _state.value = _state.value.copy(
                                thinkingByMessage = _state.value.thinkingByMessage + (thinkId to ThinkingData(
                                    steps = current?.steps ?: thinkingSteps,
                                    status = "think",
                                    rawText = thinkingSb.toString(),
                                )),
                                statusLabel = "يفكر",
                            )
                        }
                        // ⭐ الرد — يظهر حرف بحرف مباشرة (بدون clean)
                        is StreamEvent.Delta -> {
                            sb.append(ev.text)
                            val currentText = sb.toString()
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == assistantTs) {
                                        m.copy(content = currentText)
                                    } else m
                                },
                                statusLabel = "يكتب",
                            )
                        }
                        is StreamEvent.CodeFiles -> {
                            _state.value = _state.value.copy(
                                codeFiles = ev.files,
                                codeZipUrl = ev.zipUrl,
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
                            val cleanedFinal = sb.toString().trim()
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == assistantTs) {
                                        m.copy(id = finalSessionId ?: 0, content = cleanedFinal)
                                    } else m
                                },
                                currentSessionId = finalSessionId,
                                isSending = false,
                                pendingMediaType = null,  // ⭐ v2.0: تنظيف بعد انتهاء
                                statusLabel = "يفكر",
                                thinkingByMessage = newThinking,
                            )
                            finalSessionId?.let { sid ->
                                cacheRepo.saveMessages(sid, _state.value.messages)
                            }
                            // 📋 تحديث سجل الجلسات الموحّد بعد وصول الرد
                            loadAllSessions()
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

            if (!gotError && sb.isEmpty() && !gotImage) {
                _state.value = _state.value.copy(
                    messages = _state.value.messages.filterNot { it.id == -2 && it.ts == assistantTs },
                    isSending = false,
                    error = "لم يصل رد من السيرفر",
                )
            }
            } catch (e: Exception) {
                // ⭐ ضمانات: أي خطأ في الـ stream → نحرر isSending
                android.util.Log.e("ChatVM", "stream error: ${e.message}", e)
                _state.value = _state.value.copy(
                    messages = _state.value.messages.filterNot { it.id == -2 && it.ts == assistantTs },
                    isSending = false,
                    statusLabel = "يفكر",
                    error = "انقطع الاتصال — حاول مرة ثانية",
                )
            } finally {
                // ⭐ ضمان أخير: ما نسمح يبقى isSending عالق
                if (_state.value.isSending) {
                    _state.value = _state.value.copy(
                        messages = _state.value.messages.filterNot { it.id == -2 && it.ts == assistantTs },
                        isSending = false,
                    )
                }
                // تحديث السجل حتى لو فشل
                loadAllSessions()
            }
        }
    }

    /**
     * 🎨 تنقية نص الستريم من وسوم التفكير
     * - لو النموذج لسا ما وصل [[رد]] → يرجع فاضي
     * - لو في [[رد]]...[[/رد]] → يرجع النص بينهم فقط
     */
    private fun cleanStreamingText(raw: String): String {
        if (raw.isBlank()) return ""

        // ⭐ إذا فيه [[رد]] → نأخذ ما بعده فقط
        val replyStart = raw.indexOf("[[رد]]")
        var result: String = if (replyStart != -1) {
            val afterReply = raw.substring(replyStart + "[[رد]]".length)
            val replyEnd = afterReply.indexOf("[[/رد]]")
            if (replyEnd == -1) afterReply.trimStart()
            else afterReply.substring(0, replyEnd).trimStart()
        } else {
            raw
        }

        // ⭐ حماية: نحذف أي وسم متبقي
        result = result
            .replace("[[رد]]", "")
            .replace("[[/رد]]", "")
            .replace("[[تفكير]]", "")
            .replace("[[/تفكير]]", "")
            .replace("[[فكر]]", "")
            .replace("[[/فكر]]", "")
            .trim()

        return result
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
                            // 📋 تحديث سجل الجلسات الموحّد بعد رفع الملف
                            loadAllSessions()
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
            // progress حقيقي من CountingRequestBody
            when (val up = uploadRepo.uploadFile(uri, mimeType, fileName) { p ->
                updateAttachment(id) { it.copy(progress = p) }
            }) {
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

    /**
     * ينتظر حتى يكتمل رفع المرفق أو يفشل (timeout 90 ثانية)
     */
    private suspend fun awaitUpload(id: Long, timeoutMs: Long = 300_000): Attachment? {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val att = _state.value.pendingAttachments.find { it.id == id }
            if (att == null) return null  // أُزيل
            if (att.error != null) return att  // فشل
            if (att.isUploaded) return att  // تم
            kotlinx.coroutines.delay(150)
        }
        return _state.value.pendingAttachments.find { it.id == id }
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

        val sessionId = _state.value.currentSessionId

        // ⭐ ننتظر الرفع أولاً — لا نضيف أي bubble للشات
        _state.value = _state.value.copy(
            isSending = true,
            isUploading = true,
            error = null,
            statusLabel = "يرفع",
        )

        viewModelScope.launch {
          try {
            // ⭐ -1 تعني محادثة جديدة — نحوّلها لـ null
            var finalSessionId: Int? = if (sessionId == -1) null else sessionId
            var lastError: String? = null
            val uploaded: MutableList<Attachment> = mutableListOf()

            // ─── 1) ننتظر اكتمال الرفع ───
            for (att in atts) {
                val ready = awaitUpload(att.id, 300_000)
                if (ready == null) continue
                if (ready.error != null) {
                    lastError = "فشل رفع الملف: ${ready.error}"
                    break
                }
                if (!ready.isUploaded) {
                    lastError = "انتهت مدة الرفع"
                    break
                }
                uploaded.add(ready)
            }

            // ─── 2) فشل الرفع — نُبقي المرفقات في الـ composer ───
            if (lastError != null || uploaded.isEmpty()) {
                _state.value = _state.value.copy(
                    error = lastError ?: "لم يكتمل الرفع — حاول مرة أخرى",
                    isSending = false,
                    isUploading = false,
                    statusLabel = "يفكر",
                )
                return@launch
            }

            // ─── 3) نجح الرفع — الآن نضيف الرسائل للشات ───
            val firstAtt = uploaded.firstOrNull()
            val localUri = firstAtt?.uri?.toString()
            val userMsg = Message(
                id = -1,
                role = "user",
                content = caption.ifBlank { "" },
                ts = System.currentTimeMillis() / 1000.0,
                localImageUri = if (firstAtt?.mimeType?.startsWith("image/") == true) localUri else null,
            )
            val analyzingTs = System.currentTimeMillis() / 1000.0 + 1
            val analyzingMsg = Message(
                id = -2,
                role = "assistant",
                content = "",
                ts = analyzingTs,
                isAnalyzing = true,
            )
            _state.value = _state.value.copy(
                messages = _state.value.messages + userMsg + analyzingMsg,
                pendingAttachments = emptyList(),
            )

            // ─── 4) المعالجة على السيرفر ───
            for (att in uploaded) {
                val fid = att.uploadedFileId ?: continue
                when (val r = uploadRepo.processUploaded(fid, caption, finalSessionId)) {
                    is Result.Success -> {
                        val reply = r.data.reply ?: "تم"
                        finalSessionId = r.data.sessionId ?: finalSessionId
                        _state.value = _state.value.copy(
                            messages = _state.value.messages.map { m ->
                                if (m.id == -2 && m.ts == analyzingTs) {
                                    m.copy(
                                        id = finalSessionId ?: 0,
                                        content = reply,
                                        isAnalyzing = false,
                                    )
                                } else m
                            },
                            currentSessionId = finalSessionId,
                        )
                    }
                    is Result.Error -> {
                        lastError = r.message
                    }
                    else -> {}
                }
            }

            // ─── 5) خطأ في processUploaded ───
            if (lastError != null) {
                _state.value = _state.value.copy(
                    messages = _state.value.messages.filterNot { it.id == -2 && it.ts == analyzingTs },
                    error = lastError,
                )
            }

            _state.value = _state.value.copy(
                isSending = false,
                isUploading = false,
                statusLabel = "يفكر",
            )
          } catch (e: Exception) {
            // ⭐ ضمان: أي خطأ → نحرر isSending/isUploading
            android.util.Log.e("ChatVM", "sendWithAttachments error: ${e.message}", e)
            _state.value = _state.value.copy(
                isSending = false,
                isUploading = false,
                statusLabel = "يفكر",
                error = "انقطع الاتصال — حاول مرة ثانية",
            )
          } finally {
            // ⭐ ضمان أخير
            if (_state.value.isSending || _state.value.isUploading) {
                _state.value = _state.value.copy(
                    isSending = false,
                    isUploading = false,
                )
            }
            loadAllSessions()
          }
        }
    }

    fun pinMessage(msg: Message) {
        val current = _state.value.pinnedMessages
        if (current.any { it.ts == msg.ts }) {
            _state.value = _state.value.copy(pinnedMessages = current.filter { it.ts != msg.ts })
        } else {
            _state.value = _state.value.copy(pinnedMessages = (current + msg).takeLast(3))
        }
    }

    fun unpinMessage(ts: Double) {
        _state.value = _state.value.copy(
            pinnedMessages = _state.value.pinnedMessages.filter { it.ts != ts }
        )
    }

    fun isPinned(ts: Double): Boolean = _state.value.pinnedMessages.any { it.ts == ts }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
