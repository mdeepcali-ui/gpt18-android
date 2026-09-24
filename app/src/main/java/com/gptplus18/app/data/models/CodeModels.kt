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
        label = "الفريق الكامل",
        description = "5 نماذج تتعاون تلقائياً",
        emoji = "⚡",
        color = Color(0xFFC4B5FD),
    );

    companion object {
        fun fromKey(k: String?): CodeModel = AUTO
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
    @SerializedName("result") val result: CodeResult? = null,
    @SerializedName("error") val error: String? = null,
)

// ⭐ ملف جاهز للتحميل
data class CodeFile(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
    @SerializedName("size") val size: Long = 0L,
)

// ⭐ نتيجة job البرمجة الكاملة
data class CodeResult(
    @SerializedName("final_decision") val finalDecision: String? = null,
    @SerializedName("quality_score") val qualityScore: Int = 0,
    @SerializedName("summary_ar") val summaryAr: String? = null,
    @SerializedName("deliverables") val deliverables: List<String> = emptyList(),
    @SerializedName("files") val files: List<CodeFile> = emptyList(),
    @SerializedName("zip_url") val zipUrl: String? = null,
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
