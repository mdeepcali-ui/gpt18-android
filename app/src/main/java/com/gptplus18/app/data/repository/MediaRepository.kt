package com.gptplus18.app.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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

    suspend fun editImage(uri: Uri, prompt: String): Result<ImageResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.Error("فشل قراءة الصورة")
            if (bytes.size > 20 * 1024 * 1024) {
                return Result.Error("الصورة كبيرة (الحد 20MB)")
            }
            val mime = context.contentResolver.getType(uri) ?: "image/png"
            val ext = mime.substringAfterLast('/')
            val fileName = "upload_${System.currentTimeMillis()}.$ext"
            val filePart = MultipartBody.Part.createFormData(
                "file", fileName, bytes.toRequestBody(mime.toMediaTypeOrNull()),
            )
            val promptPart = (prompt.ifBlank { "حسّن الصورة" })
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val r = api.editImage(b, filePart, promptPart)
            if (r.isSuccessful) {
                val body = r.body()!!
                if (body.error != null) Result.Error(body.error)
                else Result.Success(body)
            } else when (r.code()) {
                402 -> Result.Error("تعديل الصور للمشتركين فقط")
                413 -> Result.Error("الملف كبير (الحد 20MB)")
                else -> Result.Error("فشل التعديل (${r.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }
}
