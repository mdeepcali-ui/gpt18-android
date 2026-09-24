package com.gptplus18.app.data.api

import com.gptplus18.app.BuildConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * SSE Streaming Client — يستقبل الرد حرف حرف
 */
class StreamingClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        // ⭐ 180 ثانية كحد أقصى للقراءة — يمنع التعليق للأبد
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // ⭐ ping كل 20 ثانية للتأكد من الاتصال
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    /**
     * يبعت رسالة ويرجّع Flow من chunks
     */
    fun streamMessage(
        token: String,
        sessionId: Int?,
        message: String,
        memory: Boolean = true,
    ): Flow<StreamEvent> = callbackFlow {

        val jsonBody = JSONObject().apply {
            put("message", message)
            put("memory", memory)
            if (sessionId != null) put("session_id", sessionId)
        }.toString()

        val request = Request.Builder()
            .url("${BuildConfig.BASE_URL}api/chat/stream")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "text/event-stream")
            .addHeader("Cache-Control", "no-cache")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        var doneEmitted = false

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String,
            ) {
                try {
                    val obj = JSONObject(data)
                    when {
                        // ⭐ التفكير (يُعرض في سحابة التفكير)
                        obj.has("thinking_delta") -> {
                            val td = obj.optString("thinking_delta", "")
                            if (td.isNotEmpty()) {
                                trySend(StreamEvent.ThinkingDelta(td))
                            }
                        }
                        // ⭐ الرد الفعلي (يُضاف حرف بحرف)
                        obj.has("delta") -> {
                            val delta = obj.optString("delta", "")
                            if (delta.isNotEmpty()) {
                                trySend(StreamEvent.Delta(delta))
                            }
                        }
                        obj.optBoolean("done", false) -> {
                            val sid = obj.optInt("session_id", -1)
                            val thinking = obj.optString("thinking", "")
                            doneEmitted = true
                            trySend(StreamEvent.Done(sessionId = sid, thinking = thinking))
                            close()
                        }
                        obj.has("error") -> {
                            doneEmitted = true
                            trySend(StreamEvent.Error(obj.optString("error", "خطأ")))
                            close()
                        }
                    }
                } catch (e: Exception) {
                    trySend(StreamEvent.Error(e.message ?: "خطأ parse"))
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?,
            ) {
                doneEmitted = true
                trySend(StreamEvent.Error(t?.message ?: "فشل الاتصال"))
                close()
            }

            override fun onClosed(eventSource: EventSource) {
                // ⭐ إذا SSE سكر بدون ما يبعث Done → نبعث Done يدوياً
                if (!doneEmitted) {
                    doneEmitted = true
                    trySend(StreamEvent.Done(sessionId = -1, thinking = ""))
                }
                close()
            }
        }

        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }
}

/**
 * أحداث Streaming
 */
sealed class StreamEvent {
    data class ThinkingDelta(val text: String) : StreamEvent()
    data class Delta(val text: String) : StreamEvent()
    data class Done(val sessionId: Int, val thinking: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
}
