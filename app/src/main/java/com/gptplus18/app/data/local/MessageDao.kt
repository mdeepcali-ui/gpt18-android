package com.gptplus18.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getAllMessagesSync(userId: String, sessionId: String): List<MessageEntity>

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE chat_messages SET sessionId = :newSessionId WHERE sessionId = :oldSessionId")
    suspend fun migrateSession(oldSessionId: String, newSessionId: String)

    @Query("DELETE FROM chat_messages WHERE userId = :userId AND sessionId = :sessionId")
    suspend fun clearSession(userId: String, sessionId: String)
}
