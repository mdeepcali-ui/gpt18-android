package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.api.StreamEvent
import com.gptplus18.app.data.api.StreamingClient
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {
    private val streamingClient = StreamingClient()

    private suspend fun bearer(): String? {
        val t = tokenStorage.getToken() ?: return null
        return "Bearer $t"
    }

    suspend fun listSessions(): Result<List<Session>> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.listSessions(b)
            if (r.isSuccessful) Result.Success(r.body()!!.items)
            else Result.Error("فشل تحميل الجلسات (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    // 📋 جلسات موحّدة (Chat + Code)
    suspend fun listAllSessions(): Result<List<AllSession>> {
        return try {
            val token = tokenStorage.getToken() ?: return Result.Error("غير مصرح")
            val r = api.listAllSessions("Bearer $token")
            if (r.isSuccessful) {
                Result.Success(r.body()?.sessions ?: emptyList())
            } else {
                Result.Error("خطأ (${r.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun getMessages(sid: Int): Result<List<Message>> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.getMessages(b, sid)
            if (r.isSuccessful) Result.Success(r.body()!!.items)
            else Result.Error("فشل تحميل الرسائل")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun send(sid: Int?, text: String): Result<SendMessageResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.sendMessage(b, SendMessageRequest(sid, text))
            if (r.isSuccessful) Result.Success(r.body()!!)
            else if (r.code() == 402) Result.Error("انتهت تجربتك المجانية")
            else Result.Error("فشل الإرسال (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun deleteSession(sid: Int): Result<Unit> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.deleteSession(b, sid)
            if (r.isSuccessful) Result.Success(Unit) else Result.Error("فشل الحذف")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    /**
     * Streaming — يرجّع Flow من events
     */
    fun streamMessage(
        sid: Int?,
        text: String,
        memory: Boolean = true,
    ): Flow<StreamEvent> {
        return kotlinx.coroutines.flow.flow {
            val token = tokenStorage.getToken()
            if (token.isNullOrBlank()) {
                emit(StreamEvent.Error("غير مصرح"))
                return@flow
            }
            emitAll(streamingClient.streamMessage(token, sid, text, memory))
        }
    }
}
