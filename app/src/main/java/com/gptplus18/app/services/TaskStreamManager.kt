package com.gptplus18.app.services

import android.content.Context
import android.util.Log
import com.gptplus18.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * TaskStreamManager — singleton يدير اتصال SSE بالـ backend task.
 *
 * - يفتح SSE على /api/code/task/{task_id}
 * - يبث الـ chunks عبر SharedFlow → ViewModel يشترك
 * - يحفظ آخر chunks لاستئناف فوري
 * - مستقل عن دورة حياة التطبيق
 */
object TaskStreamManager {

    private const val TAG = "TaskStreamManager"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)  // SSE لا ينتهي
        .retryOnConnectionFailure(true)
        .build()

    // الحالة
    private val _state = MutableStateFlow(TaskStreamState())
    val state: StateFlow<TaskStreamState> = _state.asStateFlow()

    // الأحداث
    private val _events = MutableSharedFlow<com.gptplus18.app.data.api.StreamEvent>(replay = 0, extraBufferCapacity = 200)
    val events: SharedFlow<com.gptplus18.app.data.api.StreamEvent> = _events.asSharedFlow()

    private var streamJob: Job? = null

    /**
     * يبدأ stream لمهمة.
     */
    fun startTask(
        context: Context,
        taskId: String,
        sessionId: Int,
        onDone: () -> Unit = {},
    ) {
        stopTask()  // اقطع القديم لو موجود
        Log.d(TAG, "startTask: $taskId")

        _state.value = TaskStreamState(
            taskId = taskId,
            sessionId = sessionId,
            isRunning = true,
            startedAt = System.currentTimeMillis(),
        )

        // نقرأ token من SharedPreferences (auth_backup) — نفس اللي يحفظه TokenStorage
        val token = context.getSharedPreferences("auth_backup", Context.MODE_PRIVATE)
            .getString("token", null)
        if (token.isNullOrBlank()) {
            Log.e(TAG, "no token")
            _state.value = _state.value.copy(isRunning = false, error = "no token")
            onDone()
            return
        }

        streamJob = scope.launch {
            try {
                val url = "${BuildConfig.BASE_URL}api/code/task/$taskId"
                val req = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .get()
                    .build()

                val response: Response = client.newCall(req).execute()
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP ${response.code}")
                    _state.value = _state.value.copy(isRunning = false, error = "HTTP ${response.code}")
                    onDone()
                    return@launch
                }

                val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
                var line: String?
                var totalChunks = 0

                while (reader.readLine().also { line = it } != null) {
                    val l = line ?: continue
                    if (l.isBlank()) continue
                    if (!l.startsWith("data: ")) continue

                    val data = l.substring(6)
                    totalChunks++
                    // نحول JSON إلى StreamEvent
                    val evt = parseSseData(data)
                    if (evt != null) {
                        _events.emit(evt)
                    }
                    _state.value = _state.value.copy(chunksCount = totalChunks)

                    if (data.contains("\"done\"")) {
                        Log.d(TAG, "done after $totalChunks chunks")
                        _state.value = _state.value.copy(
                            isRunning = false,
                            finishedAt = System.currentTimeMillis(),
                            chunksCount = totalChunks,
                        )
                        onDone()
                        return@launch
                    }
                }

                // انتهى الاتصال بدون done
                _state.value = _state.value.copy(isRunning = false)
                onDone()
            } catch (e: Exception) {
                Log.e(TAG, "stream err: ${e.message}")
                _state.value = _state.value.copy(
                    isRunning = false,
                    error = e.message ?: "unknown",
                )
                onDone()
            }
        }
    }

    /**
     * يوقف الـ stream الحالي.
     */
    /**
     * يحوّل JSON SSE إلى StreamEvent (نفس منطق StreamingClient).
     */
    private fun parseSseData(data: String): com.gptplus18.app.data.api.StreamEvent? {
        return try {
            val obj = org.json.JSONObject(data)
            when {
                obj.has("thinking_delta") -> {
                    val td = obj.optString("thinking_delta", "")
                    if (td.isNotEmpty()) com.gptplus18.app.data.api.StreamEvent.ThinkingDelta(td) else null
                }
                obj.has("status") -> {
                    val st = obj.optString("status", "")
                    if (st.isNotEmpty()) com.gptplus18.app.data.api.StreamEvent.Status(st) else null
                }
                obj.has("final_content") -> {
                    val fc = obj.optString("final_content", "")
                    if (fc.isNotEmpty()) com.gptplus18.app.data.api.StreamEvent.FinalContent(fc) else null
                }
                obj.has("delta") -> {
                    val d = obj.optString("delta", "")
                    if (d.isNotEmpty()) com.gptplus18.app.data.api.StreamEvent.Delta(d) else null
                }
                obj.optBoolean("done", false) -> {
                    val sid = obj.optInt("session_id", -1)
                    com.gptplus18.app.data.api.StreamEvent.Done(sessionId = sid, thinking = "")
                }
                obj.has("error") -> {
                    com.gptplus18.app.data.api.StreamEvent.Error(obj.optString("error", "خطأ"))
                }
                obj.has("topup_required") -> {
                    com.gptplus18.app.data.api.StreamEvent.TopupRequired(
                        obj.optString("action", "")
                    )
                }
                obj.has("save_request") -> {
                    try {
                        val sr = obj.getJSONObject("save_request")
                        val folder = sr.optString("folder_name", "project")
                        val count = sr.optInt("count", 0)
                        val totalBytes = sr.optLong("total_bytes", 0L)
                        val filesObj = sr.optJSONObject("files")
                        val filesMap = mutableMapOf<String, String>()
                        val binMap = mutableMapOf<String, com.gptplus18.app.data.api.StreamEvent.BinaryFile>()
                        if (filesObj != null) {
                            val keys = filesObj.keys()
                            while (keys.hasNext()) {
                                val k = keys.next()
                                val v = filesObj.opt(k)
                                if (v is String) filesMap[k] = v
                                else if (v is org.json.JSONObject) {
                                    val b64 = v.optString("__base64__", "")
                                    val mime = v.optString("__content_type__", "application/octet-stream")
                                    if (b64.isNotEmpty()) binMap[k] = com.gptplus18.app.data.api.StreamEvent.BinaryFile(b64, mime)
                                }
                            }
                        }
                        if (count > 0 && (filesMap.isNotEmpty() || binMap.isNotEmpty())) {
                            com.gptplus18.app.data.api.StreamEvent.SaveRequest(
                                folderName = folder,
                                files = filesMap,
                                binaries = binMap,
                                count = count,
                                totalBytes = totalBytes,
                            )
                        } else null
                    } catch (_: Exception) { null }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun stopTask() {
        streamJob?.cancel()
        streamJob = null
        _state.value = _state.value.copy(isRunning = false)
    }
}

data class TaskStreamState(
    val taskId: String = "",
    val sessionId: Int = 0,
    val isRunning: Boolean = false,
    val chunksCount: Int = 0,
    val startedAt: Long = 0L,
    val finishedAt: Long = 0L,
    val error: String = "",
)
