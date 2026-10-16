package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.api.StreamEvent
import com.gptplus18.app.data.api.StreamingClient
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton
import com.gptplus18.app.data.models.RunRequest
import com.gptplus18.app.data.models.RunResponse
import com.gptplus18.app.data.models.SaveWorkspaceRequest
import com.gptplus18.app.data.models.SaveWorkspaceResponse
import com.gptplus18.app.data.models.DetachRequest
import com.gptplus18.app.data.models.DetachResponse
import com.gptplus18.app.data.models.ActiveTasksResponse

@Singleton
class CodeRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {

    private val streamingClient = com.gptplus18.app.data.api.StreamingClient()

    // ⭐ v2.0: جلب الرصيد والاشتراك
    suspend fun getCreditsBalance(): Result<com.gptplus18.app.data.models.CreditsBalanceResponse> {
        val t = tokenStorage.getToken() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.getCreditsBalance("Bearer $t")
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل التحميل (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun streamCode(
        sessionId: Int?,
        request: String,
        modelKey: String? = null,
        fileIds: List<String> = emptyList(),
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
            fileIds = fileIds,
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

    suspend fun runCode(code: String, lang: String, sessionId: Int?): Result<RunResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeRun(b, RunRequest(code = code, lang = lang, sessionId = sessionId))
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!)
            } else {
                Result.Error("HTTP ${r.code()}: ${r.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ في التنفيذ")
        }
    }


    suspend fun saveWorkspace(folderName: String, sessionId: Int?): Result<SaveWorkspaceResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeSaveWorkspace(b, SaveWorkspaceRequest(folderName, sessionId))
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!)
            } else {
                val err = r.errorBody()?.string() ?: "HTTP ${r.code()}"
                Result.Error(err.take(200))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }


    // ⭐ v18.8: Task Persistence
    suspend fun detachStream(
        request: String,
        sessionId: Int? = null,
        modelKey: String? = null,
        fileIds: List<String>? = null,
    ): Result<DetachResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeStreamDetach(
                b,
                DetachRequest(
                    request = request,
                    sessionId = sessionId,
                    model = modelKey,
                    useTeam = (modelKey == null || modelKey == "auto"),
                    fileIds = fileIds,
                ),
            )
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!)
            } else if (r.code() == 402) {
                Result.Error("وضع البرمجة للمشتركين فقط")
            } else {
                Result.Error("فشل الإرسال (${r.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun activeTasks(): Result<ActiveTasksResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeActiveTasks(b)
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!)
            } else {
                Result.Error("HTTP ${r.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun cancelTask(taskId: String): Result<Unit> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.codeTaskCancel(b, taskId)
            if (r.isSuccessful) Result.Success(Unit) else Result.Error("HTTP ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

}
