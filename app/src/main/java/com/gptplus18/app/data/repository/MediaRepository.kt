package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {
    private suspend fun bearer(): String? {
        val t = tokenStorage.getToken() ?: return null
        return "Bearer $t"
    }

    suspend fun generateImage(prompt: String, preset: String = "square"): Result<ImageResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.generateImage(b, ImageRequest(prompt = prompt, preset = preset))
            if (r.isSuccessful) {
                val body = r.body()!!
                if (body.error != null) Result.Error(body.error)
                else Result.Success(body)
            } else if (r.code() == 402) Result.Error("توليد الصور للمشتركين فقط")
            else Result.Error("فشل التوليد (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun generateSong(prompt: String, duration: Int = 240): Result<SongResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.generateSong(b, SongRequest(prompt = prompt, duration = duration))
            if (r.isSuccessful) {
                val body = r.body()!!
                if (body.error != null) Result.Error(body.error)
                else Result.Success(body)
            } else if (r.code() == 402) Result.Error("توليد الأغاني للمشتركين فقط")
            else Result.Error("فشل التوليد (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun generateVideo(
        prompt: String,
        duration: Int = 5,
        model: String = "auto",
    ): Result<VideoResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.generateVideo(
                b,
                VideoRequest(prompt = prompt, duration = duration, model = model),
            )
            if (r.isSuccessful) {
                val body = r.body()!!
                if (body.error != null) Result.Error(body.error)
                else Result.Success(body)
            } else if (r.code() == 402) Result.Error("توليد الفيديو للمشتركين فقط")
            else Result.Error("فشل التوليد (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }
}
