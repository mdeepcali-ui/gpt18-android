package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {
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
}
