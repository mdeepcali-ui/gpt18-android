package com.gptplus18.app.ui.screens.code

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.repository.AdminRepository
import com.gptplus18.app.data.models.Attachment
import com.gptplus18.app.data.models.CodeFile
import com.gptplus18.app.data.models.CodeJobStatus
import com.gptplus18.app.data.models.CodeLogEntry
import com.gptplus18.app.data.models.CodeMessage
import com.gptplus18.app.data.models.CodeModel
import com.gptplus18.app.data.models.CodeSession
import com.gptplus18.app.data.api.StreamEvent
import com.gptplus18.app.data.repository.CodeRepository
import com.gptplus18.app.data.repository.UploadRepository
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
import com.gptplus18.app.util.ProjectSaver
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gptplus18.app.services.TaskStreamManager
import com.gptplus18.app.services.TaskStreamService
import kotlinx.coroutines.flow.collectLatest

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
    val thinkingText: String = "",
    val toolActivity: List<ToolActivity> = emptyList(),
    val pendingAttachments: List<Attachment> = emptyList(),
    // ⭐ v5.0: حقول المستخدم للـDrawer
    val userName: String = "",
    val isOwner: Boolean = false,
    val avatarUrl: String = "",
    // ⭐ v2.0: الرصيد والاشتراك (للرسالة المثبتة)
    val credits: Int = 0,
    val usdValue: Double = 0.0,
    val subscriptionPlan: String? = null,   // null | chat | code | full
    val hasCodeAccess: Boolean = false,     // عنده اشتراك برمجة أو full
    val dailyChat: Int = 0,
    val dailyCode: Int = 0,
    val dailyCodeLimit: Int = 100,
    // ⭐ v18.8: Task Persistence
    val activeTaskId: String = "",
    val isTaskRunning: Boolean = false,
    val taskChunksCount: Int = 0,
    // ⭐ v8.0: Run Popup state
    val runLoading: Boolean = false,
    val runError: String = "",
    val runOutput: String = "",
    // ⭐ v10.0: SaveRequest من المساعد
    val saveRequest: SaveRequest? = null,
    val saveInProgress: Boolean = false,
    val saveResult: String = "",
    // ⭐ v10.4: زر حفظ يدوي
    val showSaveDialog: Boolean = false,
    val manualSaveLoading: Boolean = false,
    val manualSaveError: String = "",
)

data class SaveRequest(
    val folderName: String,
    val files: Map<String, String>,
    val binaries: Map<String, ProjectSaver.BinaryFile> = emptyMap(),
    val count: Int,
    val totalBytes: Long,
)

@HiltViewModel
class CodeViewModel @Inject constructor(
    private val repo: CodeRepository,
    private val uploadRepo: UploadRepository,
    private val prefs: PreferencesRepository,
    private val analytics: AnalyticsHelper,
    private val tokenStorage: TokenStorage,
    private val memoryRepo: com.gptplus18.app.data.local.ChatMemoryRepository,
    private val adminRepo: AdminRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
) : ViewModel() {

    private val _state = MutableStateFlow(CodeUiState())

    // ⭐ v5.0: user info init (للـDrawer)
    init {
        viewModelScope.launch {
            val name = tokenStorage.getName()?.takeIf { it.isNotBlank() } ?: "المستخدم"
            val avatar = tokenStorage.getAvatar() ?: ""
            _state.value = _state.value.copy(userName = name, avatarUrl = avatar)
        }
        viewModelScope.launch {
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

        // ⭐ v18.8: استقبال chunks من TaskStreamService (حتى لو التطبيق مقفول)
        viewModelScope.launch {
            TaskStreamManager.events.collect { evt ->
                handleStreamEvent(evt)
            }
        }

        // ⭐ v18.8: تتبع حالة المهمة
        viewModelScope.launch {
            TaskStreamManager.state.collect { ts ->
                _state.value = _state.value.copy(
                    activeTaskId = ts.taskId,
                    isTaskRunning = ts.isRunning,
                    taskChunksCount = ts.chunksCount,
                )
            }
        }
    }

    /**
     * معالج موحّد للأحداث — يستخدمه streamCode المباشر + TaskStreamService.
     */
    private fun handleStreamEvent(ev: StreamEvent) {
        when (ev) {
            is StreamEvent.Status -> {
                _state.value = _state.value.copy(statusLabel = ev.text)
            }
            is StreamEvent.ToolCall -> {
                _state.value = _state.value.copy(
                    toolActivity = _state.value.toolActivity + ToolActivity(name = ev.name, args = ev.args, ok = null),
                )
            }
            is StreamEvent.ToolResult -> {
                _state.value = _state.value.copy(
                    toolActivity = _state.value.toolActivity.map { t ->
                        if (t.name == ev.name && t.ok == null) t.copy(ok = ev.ok, preview = ev.preview) else t
                    }
                )
            }
            is StreamEvent.ThinkingDelta -> {
                _state.value = _state.value.copy(
                    thinkingText = _state.value.thinkingText + ev.text,
                )
            }
            is StreamEvent.FinalContent -> {
                val lastIdx = _state.value.messages.indexOfLast { it.id == -2 }
                if (lastIdx != -1) {
                    val newMsgs = _state.value.messages.toMutableList()
                    newMsgs[lastIdx] = newMsgs[lastIdx].copy(content = ev.text)
                    _state.value = _state.value.copy(
                        messages = newMsgs,
                        isRunning = false,
                        jobStatus = "done",
                    )
                } else {
                    _state.value = _state.value.copy(isRunning = false, jobStatus = "done")
                }
            }
            is StreamEvent.Delta -> {
                val cur = _state.value
                val lastIdx = cur.messages.indexOfLast { it.id == -2 }
                if (lastIdx != -1) {
                    val newMsgs = cur.messages.toMutableList()
                    newMsgs[lastIdx] = newMsgs[lastIdx].copy(content = newMsgs[lastIdx].content + ev.text)
                    _state.value = cur.copy(messages = newMsgs)
                }
            }
            is StreamEvent.Done -> {
                val aiContent1 = _state.value.messages.lastOrNull { it.id == -2 }?.content ?: ""
                val oldKey1 = if ((_state.value.currentSessionId ?: 0) > 0) _state.value.currentSessionId.toString() else "pending"
                val newKey1 = if (ev.sessionId > 0) ev.sessionId.toString() else oldKey1
                _state.value = _state.value.copy(
                    isRunning = false,
                    jobStatus = "done",
                )
                if (ev.sessionId > 0) {
                    _state.value = _state.value.copy(currentSessionId = ev.sessionId)
                }
                if (aiContent1.isNotBlank()) {
                    viewModelScope.launch {
                        try {
                            val u1 = tokenStorage.getUid().toString()
                            if (oldKey1 == "pending" && newKey1 != "pending") memoryRepo.migrateSession(u1, "pending", newKey1)
                            memoryRepo.appendAssistantMessage(u1, newKey1, aiContent1)
                        } catch (_: Exception) {}
                    }
                }
            }
            is StreamEvent.Error -> {
                _state.value = _state.value.copy(
                    isRunning = false,
                    error = ev.message,
                    jobStatus = "error",
                )
            }
            is StreamEvent.SaveRequest -> {
                // تحويل بين نوعي BinaryFile
                val binConv = ev.binaries.mapValues { (_, bf) ->
                    com.gptplus18.app.util.ProjectSaver.BinaryFile(bf.base64, bf.mimeType)
                }
                onSaveRequest(
                    SaveRequest(
                        folderName = ev.folderName,
                        files = ev.files,
                        binaries = binConv,
                        count = ev.count,
                        totalBytes = ev.totalBytes,
                    )
                )
            }
            is StreamEvent.TopupRequired -> {
                // لا شيء — يُعالج في UI
            }
            else -> {}
        }
    }

    // ⏹ Job الـ streaming
    private var streamJob: kotlinx.coroutines.Job? = null
    val state: StateFlow<CodeUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    init {
        loadSessions()
    }

    // ⭐ v2.0: تحميل الرصيد والاشتراك
    fun loadCredits() {
        viewModelScope.launch {
            try {
                when (val r = repo.getCreditsBalance()) {
                    is Result.Success -> {
                        val d = r.data
                        val sub = d.subscription
                        _state.value = _state.value.copy(
                            credits = d.credits,
                            usdValue = d.usdValue,
                            subscriptionPlan = sub?.plan,
                            hasCodeAccess = (sub?.plan == "code" || sub?.plan == "full"),
                            dailyChat = d.dailyUsage?.chat ?: 0,
                            dailyCode = d.dailyUsage?.code ?: 0,
                            dailyCodeLimit = d.dailyUsage?.codeLimit ?: 100,
                        )
                    }
                    is Result.Error -> {
                        // صامت — ما نعطل المستخدم
                    }
                    else -> {}
                }
            } catch (_: Exception) { }
        }
    }

    fun refreshAvatar() {
        viewModelScope.launch {
            try {
                val name = tokenStorage.getName()?.takeIf { it.isNotBlank() } ?: "المستخدم"
                val avatar = tokenStorage.getAvatar() ?: ""
                _state.value = _state.value.copy(userName = name, avatarUrl = avatar)
            } catch (_: Exception) { }
            // ⭐ v2.0: تحميل الرصيد كل مرة
            loadCredits()
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


    // ═══════════════════════════════════════════════════════════
    //  📎 المرفقات (نسخة من ChatViewModel)
    // ═══════════════════════════════════════════════════════════

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
            else -> "التفكير"
        }
        _state.value = _state.value.copy(
            pendingAttachments = _state.value.pendingAttachments + att,
            statusLabel = newLabel,
        )
        viewModelScope.launch {
            when (val up = uploadRepo.uploadFile(uri, mimeType, fileName) { p ->
                updateAttachment(id) { it.copy(progress = p) }
            }) {
                is Result.Success -> updateAttachment(id) {
                    it.copy(progress = 1f, uploadedFileId = up.data.fileId)
                }
                is Result.Error -> updateAttachment(id) { it.copy(error = up.message) }
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

    private suspend fun awaitUpload(id: Long, timeoutMs: Long = 300_000): Attachment? {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val att = _state.value.pendingAttachments.find { it.id == id }
            if (att == null) return null
            if (att.error != null) return att
            if (att.isUploaded) return att
            kotlinx.coroutines.delay(150)
        }
        return _state.value.pendingAttachments.find { it.id == id }
    }

    fun submitWithAttachments(request: String) {
        if (_state.value.isRunning) return
        val atts = _state.value.pendingAttachments
        if (atts.isEmpty()) {
            if (request.isNotBlank()) submit(request)
            return
        }

        // ⭐ ننتظر الرفع أولاً
        val sid = _state.value.currentSessionId
        val now = System.currentTimeMillis() / 1000.0
        // ⭐ v9.1: نحول المرفقات لـ LocalAttachment عشان تنعرض بعد الإرسال
        val localAtts = atts.map { a ->
            com.gptplus18.app.data.models.LocalAttachment(
                name = a.fileName,
                mime = a.mimeType,
                isImage = a.isImage,
                localUri = a.uri.toString(),
            )
        }
        // ⭐ v9.2: نجمع كل الصور لـ localImageUris أيضاً (لعرضها في MessageBubble)
        val localImageUris = atts.filter { it.isImage }.map { it.uri.toString() }
        val _userText = if (request.isBlank()) "" else request
        val tempUser = CodeMessage(
            id = -1, role = "user", content = _userText, ts = now,
            localAttachments = localAtts,
            localImageUris = localImageUris.ifEmpty { null },
        )
        val tempAssistant = CodeMessage(id = -2, role = "assistant", content = "", ts = now + 1)

        _state.value = _state.value.copy(
            messages = _state.value.messages + tempUser + tempAssistant,
            isRunning = true,
            error = null,
            logs = emptyList(),
            liveOutput = "",
            jobStatus = "starting",
            statusLabel = "يرفع",
        )

        viewModelScope.launch {
            try {
                // ننتظر كل المرفقات
                val uploadedIds = mutableListOf<String>()
                for (att in atts) {
                    val done = awaitUpload(att.id)
                    if (done?.uploadedFileId != null) {
                        uploadedIds.add(done.uploadedFileId!!)
                    } else if (done?.error != null) {
                        _state.value = _state.value.copy(
                            isRunning = false,
                            error = "فشل رفع: ${done.error}",
                            pendingAttachments = emptyList(),
                        )
                        return@launch
                    }
                }

                // ⭐ ننظف المرفقات من الصندوق فوراً بعد الرفع
                _state.value = _state.value.copy(pendingAttachments = emptyList())

                // نبني الطلب النهائي مع الـfile_ids
                val finalReq = if (request.isBlank()) "حلل الملفات المرفقة" else request
                val sidForApi = if (sid == -1) null else sid

                // ⭐ v19.0: نستخدم Detached Stream (البث بالخلفية) ليكمل حتى لو طلعت من الشاشة
                submitDetached(finalReq, uploadedIds)
                return@launch
                
                // (الكود القديم — لن يتم الوصول له)
                repo.streamCode(sidForApi, finalReq, fileIds = uploadedIds).collect { ev ->
                    when (ev) {
                        is StreamEvent.SaveRequest -> {
                            val binariesConverted = ev.binaries.mapValues { (_, bf) ->
                                ProjectSaver.BinaryFile(bf.base64, bf.mimeType)
                            }
                            onSaveRequest(
                                SaveRequest(
                                    folderName = ev.folderName,
                                    files = ev.files,
                                    binaries = binariesConverted,
                                    count = ev.count,
                                    totalBytes = ev.totalBytes,
                                )
                            )
                        }
                        is StreamEvent.Delta -> {
                            val newText = _state.value.liveOutput + ev.text
                            _state.value = _state.value.copy(
                                liveOutput = newText,
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.ts == tempAssistant.ts) m.copy(content = newText) else m
                                },
                            )
                        }
                        is StreamEvent.Status -> {
                            _state.value = _state.value.copy(statusLabel = ev.text)
                        }
                        is StreamEvent.ToolCall -> {
                _state.value = _state.value.copy(
                    toolActivity = _state.value.toolActivity + ToolActivity(name = ev.name, args = ev.args, ok = null),
                )
            }
            is StreamEvent.ToolResult -> {
                _state.value = _state.value.copy(
                    toolActivity = _state.value.toolActivity.map { t ->
                        if (t.name == ev.name && t.ok == null) t.copy(ok = ev.ok, preview = ev.preview) else t
                    }
                )
            }
            is StreamEvent.ThinkingDelta -> {
                            _state.value = _state.value.copy(thinkingText = _state.value.thinkingText + ev.text)
                        }
                        is StreamEvent.Done -> {
                            val aiContent2 = _state.value.messages.lastOrNull { it.id == -2 }?.content ?: ""
                            val oldKey2 = if ((_state.value.currentSessionId ?: 0) > 0) _state.value.currentSessionId.toString() else "pending"
                            val newKey2 = if (ev.sessionId > 0) ev.sessionId.toString() else oldKey2
                            _state.value = _state.value.copy(
                                isRunning = false,
                                pendingAttachments = emptyList(),
                                currentSessionId = ev.sessionId,
                                statusLabel = "",
                                thinkingText = "",
                toolActivity = emptyList(),
                                // ⭐ تحويل placeholder -2 إلى رسالة نهائية (id=0)
                                messages = _state.value.messages.map { m ->
                                    if (m.id == -2 && m.content.isNotBlank()) m.copy(id = 0) else m
                                },
                            )
                            if (aiContent2.isNotBlank()) {
                                viewModelScope.launch {
                                    try {
                                        val u2 = tokenStorage.getUid().toString()
                                        if (oldKey2 == "pending" && newKey2 != "pending") memoryRepo.migrateSession(u2, "pending", newKey2)
                                        memoryRepo.appendAssistantMessage(u2, newKey2, aiContent2)
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                        is StreamEvent.Error -> {
                            _state.value = _state.value.copy(
                                isRunning = false, error = ev.message,
                                pendingAttachments = emptyList(),
                            )
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRunning = false,
                    error = e.message ?: "خطأ",
                    pendingAttachments = emptyList(),
                )
            } finally {
                // ⭐ ضمان: أي خطأ → نظف المرفقات
                if (_state.value.pendingAttachments.isNotEmpty()) {
                    _state.value = _state.value.copy(pendingAttachments = emptyList())
                }
            }
        }
    }

    /**
     * ⭐ v18.8: submit مع Task Persistence — الرد يكمل لو المستخدم خرج.
     */
    fun submitDetached(request: String, fileIds: List<String>? = null) {
        if (request.isBlank()) return
        if (_state.value.isRunning) return

        val sid = _state.value.currentSessionId
        val sidForApi = if (sid == -1) null else sid
        val model = _state.value.currentModel
        val sessionKey = if ((sid ?: 0) > 0) sid.toString() else "pending"

        // رسائل مؤقتة
        val now = System.currentTimeMillis() / 1000.0
        val tempUser = CodeMessage(id = -1, role = "user", content = request, ts = now)
        val tempAssistant = CodeMessage(id = -2, role = "assistant", content = "", ts = now + 1)

        val cleanMessages = _state.value.messages.filterNot { it.id == -2 && it.content.isBlank() }
        _state.value = _state.value.copy(
            messages = cleanMessages + tempUser + tempAssistant,
            isRunning = true,
            error = null,
            logs = emptyList(),
            liveOutput = "",
            jobStatus = "starting",
            statusLabel = "بدء المهمة",
            thinkingText = "",
                toolActivity = emptyList(),
        )

        viewModelScope.launch {
            val uid = try { tokenStorage.getUid().toString() } catch (_: Exception) { "0" }
            val history = try {
                memoryRepo.appendUserMessage(uid, sessionKey, request)
                val all = memoryRepo.getHistory(uid, sessionKey)
                if (all.isNotEmpty() && all.last().role == "user" && all.last().content == request) all.dropLast(1) else all
            } catch (_: Exception) { emptyList() }

            when (val r = repo.detachStream(
                request = request,
                sessionId = sidForApi,
                modelKey = model.key,
                fileIds = fileIds,
                history = history,
            )) {
                is Result.Success -> {
                    val resp = r.data
                    if (resp.success && resp.taskId.isNotBlank()) {
                        if (sessionKey == "pending" && resp.sessionId > 0) {
                            try { memoryRepo.migrateSession(uid, "pending", resp.sessionId.toString()) } catch (_: Exception) {}
                        }
                        _state.value = _state.value.copy(
                            activeTaskId = resp.taskId,
                            jobStatus = "running",
                        )
                        // نشغل الـ foreground service
                        TaskStreamService.start(
                            context = appContext,
                            taskId = resp.taskId,
                            sessionId = resp.sessionId,
                            title = request.take(50),
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isRunning = false,
                            error = resp.detail.ifBlank { "فشل بدء المهمة" },
                            jobStatus = "error",
                        )
                    }
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isRunning = false,
                        error = r.message ?: "خطأ",
                        jobStatus = "error",
                    )
                }
            }
        }
    }

    /**
     * ⭐ v18.8: يتحقق لو في مهمة شغالة عند العودة.
     */
    fun checkActiveTasks() {
        viewModelScope.launch {
            when (val r = repo.activeTasks()) {
                is Result.Success -> {
                    val tasks = r.data.tasks.filter { it.status == "running" }
                    if (tasks.isNotEmpty()) {
                        val t = tasks.first()
                        _state.value = _state.value.copy(
                            activeTaskId = t.id,
                            isTaskRunning = true,
                            taskChunksCount = t.chunksCount,
                        )
                        // إعادة ربط
                        TaskStreamService.start(
                            context = appContext,
                            taskId = t.id,
                            sessionId = t.sessionId,
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun submit(request: String) {
        if (request.isBlank()) return
        if (_state.value.isRunning) return

        // ⭐ v19.1: نوجه الطلب مباشرة للباك-إند بدون تعيين isRunning مسبقاً لمنع التعليق
        submitDetached(request)
        return
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


    /**
     * ⏹ إيقاف الرد الجاري
     */
    fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        val taskId = _state.value.activeTaskId
        if (!taskId.isNullOrBlank()) {
            viewModelScope.launch {
                try { repo.cancelTask(taskId) } catch (_: Exception) {}
            }
        }
        try { TaskStreamManager.stopTask() } catch (_: Exception) {}
        val msgs = _state.value.messages.map { m ->
            if (m.id == -2) m.copy(id = 0) else m
        }
        _state.value = _state.value.copy(
            messages = msgs,
            isRunning = false,
            statusLabel = "",
            thinkingText = "",
            toolActivity = emptyList(),
            activeTaskId = "",
        )
        android.util.Log.d("CodeVM", "stopStreaming() — server cancel + local reset")
    }


    // ⭐ v8.0: تنفيذ كود من Popup Run
    fun runCode(code: String, lang: String) {
        if (code.isBlank()) return
        _state.value = _state.value.copy(runLoading = true, runError = "", runOutput = "")
        viewModelScope.launch {
            when (val r = repo.runCode(code, lang, _state.value.currentSessionId)) {
                is Result.Success -> {
                    val resp = r.data
                    _state.value = _state.value.copy(
                        runLoading = false,
                        runError = if (resp.success) "" else (resp.error.ifBlank { "فشل التنفيذ" }),
                        runOutput = resp.output,
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        runLoading = false,
                        runError = r.message ?: "خطأ في التنفيذ",
                        runOutput = "",
                    )
                }
            }
        }
    }

    fun clearRunState() {
        _state.value = _state.value.copy(runLoading = false, runError = "", runOutput = "")
    }


    // ⭐ v10.0: save_to_phone handlers
    fun onSaveRequest(request: SaveRequest) {
        _state.value = _state.value.copy(saveRequest = request, saveResult = "")
    }

    fun markSaveInProgress() {
        _state.value = _state.value.copy(saveInProgress = true)
    }

    fun onSaveSuccess(count: Int, folderPath: String) {
        _state.value = _state.value.copy(
            saveRequest = null,
            saveInProgress = false,
            saveResult = "✅ تم حفظ $count ملف في $folderPath",
        )
    }

    fun onSaveError(msg: String) {
        _state.value = _state.value.copy(
            saveInProgress = false,
            saveResult = "❌ فشل الحفظ: $msg",
        )
    }

    fun clearSaveResult() {
        _state.value = _state.value.copy(saveResult = "")
    }


    // ⭐ v10.4: حفظ يدوي
    fun showSaveDialog() {
        _state.value = _state.value.copy(showSaveDialog = true, manualSaveError = "")
    }

    fun hideSaveDialog() {
        _state.value = _state.value.copy(showSaveDialog = false, manualSaveError = "")
    }

    fun manualSaveWorkspace(folderName: String) {
        _state.value = _state.value.copy(manualSaveLoading = true, manualSaveError = "")
        viewModelScope.launch {
            when (val r = repo.saveWorkspace(folderName, _state.value.currentSessionId)) {
                is Result.Success -> {
                    val resp = r.data
                    if (resp.success && resp.files.isNotEmpty()) {
                        // نحوّل لـ SaveRequest ليستخدم ProjectSaver نفسه
                        _state.value = _state.value.copy(
                            manualSaveLoading = false,
                            showSaveDialog = false,
                            saveRequest = SaveRequest(
                                folderName = resp.folderName,
                                files = resp.files,
                                binaries = emptyMap(),
                                count = resp.count,
                                totalBytes = resp.totalBytes,
                            ),
                        )
                    } else {
                        _state.value = _state.value.copy(
                            manualSaveLoading = false,
                            manualSaveError = resp.detail.ifBlank { "لا ملفات" },
                        )
                    }
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        manualSaveLoading = false,
                        manualSaveError = r.message ?: "خطأ",
                    )
                }
            }
        }
    }

}

data class ToolActivity(
    val name: String,
    val args: String = "",
    val ok: Boolean? = null,   // null = pending, true = success, false = failed
    val preview: String = "",
)

