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
        .readTimeout(0, TimeUnit.MILLISECONDS)   // ما ننتهي تلقائياً
        .writeTimeout(30, TimeUnit.SECONDS)
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
                        obj.has("delta") -> {
                            val delta = obj.optString("delta", "")
                            if (delta.isNotEmpty()) {
                                trySend(StreamEvent.Delta(delta))
                            }
                        }
                        obj.optBoolean("done", false) -> {
                            val sid = obj.optInt("session_id", -1)
                            val thinking = obj.optString("thinking", "")
                            trySend(StreamEvent.Done(sessionId = sid, thinking = thinking))
                            close()
                        }
                        obj.has("error") -> {
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
                trySend(StreamEvent.Error(t?.message ?: "فشل الاتصال"))
                close()
            }

            override fun onClosed(eventSource: EventSource) {
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
    data class Delta(val text: String) : StreamEvent()
    data class Done(val sessionId: Int, val thinking: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
}
