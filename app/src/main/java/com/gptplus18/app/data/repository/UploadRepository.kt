package com.gptplus18.app.data.repository

import android.content.Context
import android.net.Uri
import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.ProcessUploadRequest
import com.gptplus18.app.data.models.ProcessUploadResponse
import com.gptplus18.app.data.models.UploadTempResponse
import com.gptplus18.app.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    @ApplicationContext private val context: Context,
) {
    private suspend fun bearer(): String? {
        val t = tokenStorage.getToken() ?: return null
        return "Bearer $t"
    }

    suspend fun uploadFile(uri: Uri, mimeType: String, fileName: String): Result<UploadTempResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.Error("تعذر فتح الملف")
                val bytes = inputStream.readBytes()
                inputStream.close()

                if (bytes.size > 100 * 1024 * 1024) {
                    return@withContext Result.Error("الملف أكبر من 100MB")
                }

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

                val r = api.uploadTemp(b, part)
                if (r.isSuccessful) Result.Success(r.body()!!)
                else Result.Error("فشل الرفع (${r.code()})")
            } catch (e: Exception) {
                Result.Error(e.message ?: "خطأ رفع")
            }
        }
    }

    suspend fun processUploaded(fileId: String, caption: String, sessionId: Int?): Result<ProcessUploadResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.processUploaded(b, ProcessUploadRequest(fileId, caption, sessionId))
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل المعالجة (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }
}
