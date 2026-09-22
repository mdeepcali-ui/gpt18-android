package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

data class UploadTempResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("file_id") val fileId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("size") val size: Long = 0,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("is_image") val isImage: Boolean = false,
)

data class ProcessUploadRequest(
    @SerializedName("file_id") val fileId: String,
    @SerializedName("caption") val caption: String = "",
    @SerializedName("session_id") val sessionId: Int? = null,
)

data class ProcessUploadResponse(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("reply") val reply: String? = null,
    @SerializedName("action") val action: String? = null,
)
