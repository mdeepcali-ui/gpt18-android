package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

data class RunRequest(
    @SerializedName("code") val code: String,
    @SerializedName("lang") val lang: String = "python",
    @SerializedName("session_id") val sessionId: Int? = null,
)

data class RunResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("output") val output: String = "",
    @SerializedName("error") val error: String = "",
    @SerializedName("duration_ms") val durationMs: Double = 0.0,
    @SerializedName("lang") val lang: String = "python",
    @SerializedName("session_id") val sessionId: Int = 0,
)


data class SaveWorkspaceRequest(
    @SerializedName("folder_name") val folderName: String,
    @SerializedName("session_id") val sessionId: Int? = null,
)

data class SaveWorkspaceResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("folder_name") val folderName: String = "",
    @SerializedName("files") val files: Map<String, String> = emptyMap(),
    @SerializedName("count") val count: Int = 0,
    @SerializedName("total_bytes") val totalBytes: Long = 0,
    @SerializedName("detail") val detail: String = "",
)


// ⭐ v18.8: Task Persistence
data class MessageDto(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
)

data class DetachRequest(
    @SerializedName("request") val request: String,
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("use_team") val useTeam: Boolean = true,
    @SerializedName("file_ids") val fileIds: List<String>? = null,
    @SerializedName("history") val history: List<MessageDto> = emptyList(),
)

data class DetachResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("task_id") val taskId: String = "",
    @SerializedName("session_id") val sessionId: Int = 0,
    @SerializedName("detail") val detail: String = "",
)

data class ActiveTaskItem(
    @SerializedName("id") val id: String,
    @SerializedName("session_id") val sessionId: Int = 0,
    @SerializedName("status") val status: String = "running",
    @SerializedName("chunks_count") val chunksCount: Int = 0,
    @SerializedName("started_at") val startedAt: Double = 0.0,
    @SerializedName("finished_at") val finishedAt: Double = 0.0,
)

data class ActiveTasksResponse(
    @SerializedName("tasks") val tasks: List<ActiveTaskItem> = emptyList(),
    @SerializedName("count") val count: Int = 0,
)
