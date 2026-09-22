package com.gptplus18.app.data.models

import android.net.Uri

data class Attachment(
    val id: Long,
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val progress: Float = 0f,       // 0..1
    val uploadedFileId: String? = null, // بعد الرفع
    val error: String? = null,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isVideo: Boolean get() = mimeType.startsWith("video/")
    val isAudio: Boolean get() = mimeType.startsWith("audio/")
    val isPdf: Boolean get() = mimeType.contains("pdf")
    val isUploaded: Boolean get() = uploadedFileId != null
    val isUploading: Boolean get() = progress > 0f && progress < 1f && error == null
}
