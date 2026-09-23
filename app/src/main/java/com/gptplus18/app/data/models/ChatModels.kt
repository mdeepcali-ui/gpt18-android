package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

data class Session(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("created_at") val createdAt: Double,
    @SerializedName("updated_at") val updatedAt: Double,
    @SerializedName("msg_count") val msgCount: Int = 0,
    @SerializedName("last_msg") val lastMsg: String? = null,
)

data class Message(
    @SerializedName("id") val id: Int,
    @SerializedName("role") val role: String,  // "user" | "assistant"
    @SerializedName("content") val content: String,
    @SerializedName("ts") val ts: Double,
)

data class SessionsList(
    @SerializedName("items") val items: List<Session> = emptyList(),
)

data class MessagesList(
    @SerializedName("items") val items: List<Message> = emptyList(),
)

data class SendMessageRequest(
    @SerializedName("session_id") val sessionId: Int?,
    @SerializedName("message") val message: String,
    @SerializedName("memory") val memory: Boolean = true,
)

data class SendMessageResponse(
    @SerializedName("session_id") val sessionId: Int,
    @SerializedName("reply") val reply: String,
    @SerializedName("thinking") val thinking: String? = null,
    @SerializedName("limited") val limited: Boolean = false,
)

// ═══════════════════════════════════════════════
// 📋 All Sessions — Chat + Code (موحّد)
// ═══════════════════════════════════════════════
data class AllSession(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,       // "chat" | "code"
    @SerializedName("title") val title: String,
    @SerializedName("last_msg") val lastMsg: String? = null,
    @SerializedName("msg_count") val msgCount: Int = 0,
    @SerializedName("created_at") val createdAt: Double,
    @SerializedName("updated_at") val updatedAt: Double,
)

data class AllSessionsList(
    @SerializedName("sessions") val sessions: List<AllSession> = emptyList(),
)
