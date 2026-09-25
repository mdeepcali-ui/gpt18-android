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
        imageUrl: String? = null,
    ): Result<VideoResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.generateVideo(
                b,
                VideoRequest(prompt = prompt, duration = duration, model = model, imageUrl = imageUrl),
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

    
    // ⭐ M4-b: سجل الوسائط
    suspend fun getHistory(limit: Int = 50): Result<MediaHistoryResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.getMediaHistory(b, limit)
            if (r.isSuccessful) {
                val body = r.body() ?: return Result.Error("رد فارغ")
                Result.Success(body)
            } else Result.Error("فشل تحميل السجل (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }
    
    suspend fun deleteHistoryItem(id: Int): Result<Boolean> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.deleteMediaHistoryItem(b, id)
            if (r.isSuccessful) Result.Success(true)
            else Result.Error("فشل الحذف (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }
    
    suspend fun uploadTempImage(localUri: String): Result<String> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val ctx = com.gptplus18.app.GptPlus18App.appContext
            val uri = android.net.Uri.parse(localUri)
            val input = ctx.contentResolver.openInputStream(uri)
                ?: return Result.Error("فشل قراءة الصورة")
            val bytes = input.readBytes()
            input.close()

            val file = java.io.File(ctx.cacheDir, "video_input_${System.currentTimeMillis()}.jpg")
            file.writeBytes(bytes)

            val reqFile = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("image/jpeg"),
                file,
            )
            val part = okhttp3.MultipartBody.Part.createFormData("file", file.name, reqFile)

            val r = api.uploadTempFile(b, part)
            if (r.isSuccessful) {
                val url = r.body()?.get("url") as? String
                if (url != null) Result.Success(url)
                else Result.Error("استجابة غير متوقعة")
            } else Result.Error("فشل الرفع (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ رفع")
        }
    }

    suspend fun clearHistory(): Result<Int> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.clearMediaHistory(b)
            if (r.isSuccessful) {
                @Suppress("UNCHECKED_CAST")
                val n = (r.body()?.get("deleted") as? Number)?.toInt() ?: 0
                Result.Success(n)
            } else Result.Error("فشل المسح (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

}
