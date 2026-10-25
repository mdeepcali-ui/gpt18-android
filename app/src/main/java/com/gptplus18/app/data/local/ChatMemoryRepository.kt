package com.gptplus18.app.data.local

import android.content.Context
import com.gptplus18.app.data.models.MessageDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMemoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @Volatile private var lastSavedHash: Int = 0

    private fun dao(userId: String): MessageDao =
        AppDatabase.getDatabase(context, userId).messageDao()

    suspend fun appendUserMessage(userId: String, sessionId: String, content: String) {
        dao(userId).insertMessage(
            MessageEntity(userId = userId, sessionId = sessionId, role = "user", content = content)
        )
    }

    suspend fun appendAssistantMessage(userId: String, sessionId: String, content: String) {
        if (content.isBlank()) return
        val h = content.hashCode() xor sessionId.hashCode()
        if (h == lastSavedHash) return
        lastSavedHash = h
        dao(userId).insertMessage(
            MessageEntity(userId = userId, sessionId = sessionId, role = "assistant", content = content)
        )
    }

    suspend fun getHistory(userId: String, sessionId: String): List<MessageDto> {
        return dao(userId).getAllMessagesSync(userId, sessionId)
            .map { MessageDto(role = it.role, content = it.content) }
    }

    suspend fun migrateSession(userId: String, oldSessionId: String, newSessionId: String) {
        dao(userId).migrateSession(oldSessionId, newSessionId)
    }

    suspend fun clearSession(userId: String, sessionId: String) {
        dao(userId).clearSession(userId, sessionId)
    }
}
