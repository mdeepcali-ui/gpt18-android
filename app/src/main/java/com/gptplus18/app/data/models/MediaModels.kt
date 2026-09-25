package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

data class ImageRequest(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("preset") val preset: String = "square",
)

data class ImageResponse(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("reply") val reply: String? = null,
    @SerializedName("error") val error: String? = null,
)

data class SongRequest(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("duration") val duration: Int = 240,
)

data class SongResponse(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("audio_url") val audioUrl: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("lyrics") val lyrics: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("error") val error: String? = null,
)

data class VideoRequest(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("duration") val duration: Int = 5,
    @SerializedName("model") val model: String = "auto",
    @SerializedName("image_url") val imageUrl: String? = null,
)

data class VideoResponse(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("model_used") val modelUsed: String? = null,
    @SerializedName("reply") val reply: String? = null,
    @SerializedName("error") val error: String? = null,
)

// ⭐ M4-b: سجل الوسائط
data class MediaHistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,       // image/song/video
    @SerializedName("prompt") val prompt: String? = null,
    @SerializedName("url") val url: String,
    @SerializedName("preset") val preset: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("created_at") val createdAt: Double,
)

data class MediaHistoryResponse(
    @SerializedName("items") val items: List<MediaHistoryItem> = emptyList(),
    @SerializedName("count") val count: Int = 0,
)
