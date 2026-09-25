package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.api.StreamEvent
import com.gptplus18.app.data.api.StreamingClient
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {

    private val streamingClient = com.gptplus18.app.data.api.StreamingClient()

    fun streamCode(
        sessionId: Int?,
        request: String,
        modelKey: String? = null,
    ): kotlinx.coroutines.flow.Flow<StreamEvent> {
        val token = tokenStorage.getToken() ?: ""
        val useTeam = modelKey == null || modelKey == "auto"
        val model = if (useTeam) null else modelKey
        return streamingClient.streamCode(
            token = token,
            sessionId = sessionId,
            request = request,
            model = model,
            useTeam = useTeam,
        )
    }
    private suspend fun bearer(): String? {
        val t = tokenStorage.getToken() ?: return null
        return "Bearer $t"
    }

    suspend fun generate(
        request: String,
        sessionId: Int? = null,
        modelKey: String? = null,
    ): Result<CodeJobStart> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        val useTeam = modelKey == null || modelKey == "auto"
        return try {
            val r = api.codeGenerate(
                b,
                CodeRequest(
                    request = request,
                    sessionId = sessionId,
                    model = if (useTeam) null else modelKey,
                    useTeam = useTeam,
                ),
            )
            if (r.isSuccessful) Result.Success(r.body()!!)
            else if (r.code() == 402) Result.Error("وضع البرمجة للمشتركين فقط")
            else Result.Error("فشل الإرسال (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun getJobStatus(jobId: String): Result<CodeJobStatus> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeStatus(b, jobId)
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل الفحص")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun listSessions(): Result<List<CodeSession>> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeSessions(b)
            if (r.isSuccessful) Result.Success(r.body()!!.sessions)
            else Result.Error("فشل التحميل")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun getMessages(sid: Int): Result<List<CodeMessage>> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeMessages(b, sid)
            if (r.isSuccessful) Result.Success(r.body()!!.messages)
            else Result.Error("فشل التحميل")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun deleteSession(sid: Int): Result<Unit> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeDeleteSession(b, sid)
            if (r.isSuccessful) Result.Success(Unit) else Result.Error("فشل الحذف")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }
}
