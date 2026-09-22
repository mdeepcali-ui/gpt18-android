package com.gptplus18.app.data.models

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════
// نماذج وضع البرمجة
// ═══════════════════════════════════════════
enum class CodeModel(
    val key: String,
    val label: String,
    val description: String,
    val emoji: String,
    val color: Color,
) {
    AUTO(
        key = "auto",
        label = "Auto — الفريق الكامل",
        description = "5 نماذج تتعاون: Claude + GLM + Qwen + Kimi + MiniMax",
        emoji = "⚡",
        color = Color(0xFFC4B5FD),
    ),
    CLAUDE(
        key = "anthropic/claude-sonnet-5",
        label = "Claude Sonnet 5",
        description = "القائد — يفكر ويخطط",
        emoji = "👑",
        color = Color(0xFFFFC58F),
    ),
    QWEN(
        key = "qwen/qwen3-coder-plus",
        label = "Qwen3 Coder Plus",
        description = "متخصص بالبرمجة",
        emoji = "💻",
        color = Color(0xFF93E0FF),
    ),
    GLM(
        key = "z-ai/glm-5.3",
        label = "GLM 5.3",
        description = "المخطط المعماري",
        emoji = "🏗️",
        color = Color(0xFF7EE787),
    ),
    KIMI(
        key = "moonshotai/kimi-k3",
        label = "Kimi K3",
        description = "المدقق",
        emoji = "🔍",
        color = Color(0xFFA78BFA),
    ),
    MINIMAX(
        key = "minimax/minimax-m3",
        label = "MiniMax M3",
        description = "متخصص بالواجهات",
        emoji = "🎨",
        color = Color(0xFFFF9EC7),
    );

    companion object {
        fun fromKey(k: String?): CodeModel = values().firstOrNull { it.key == k } ?: AUTO
    }
}

// ═══════════════════════════════════════════
// Requests / Responses
// ═══════════════════════════════════════════
data class CodeRequest(
    @SerializedName("request") val request: String,
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("model") val model: String? = null,  // ← جديد
    @SerializedName("use_team") val useTeam: Boolean = true,  // ← جديد
)

data class CodeJobStart(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("session_id") val sessionId: Int,
    @SerializedName("status") val status: String,
)

data class CodeLogEntry(
    @SerializedName("msg") val msg: String,
    @SerializedName("type") val type: String,
)

data class CodeJobStatus(
    @SerializedName("status") val status: String,
    @SerializedName("log") val log: List<CodeLogEntry> = emptyList(),
    @SerializedName("result") val result: Any? = null,
    @SerializedName("error") val error: String? = null,
)

data class CodeSession(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("created_at") val createdAt: Double,
    @SerializedName("updated_at") val updatedAt: Double,
    @SerializedName("last_request") val lastRequest: String? = null,
    @SerializedName("model_used") val modelUsed: String? = null,
)

data class CodeSessionsList(
    @SerializedName("sessions") val sessions: List<CodeSession> = emptyList(),
)

data class CodeMessage(
    @SerializedName("id") val id: Int,
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
    @SerializedName("meta") val meta: String? = null,
    @SerializedName("ts") val ts: Double,
)

data class CodeMessagesList(
    @SerializedName("messages") val messages: List<CodeMessage> = emptyList(),
)
