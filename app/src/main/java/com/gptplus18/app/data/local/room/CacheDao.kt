package com.gptplus18.app.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {

    // ─── Sessions ───
    @Query("SELECT * FROM cached_sessions ORDER BY updatedAt DESC")
    fun observeSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM cached_sessions ORDER BY updatedAt DESC")
    suspend fun getSessions(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

    @Query("DELETE FROM cached_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM cached_sessions WHERE id = :sid")
    suspend fun deleteSession(sid: Int)

    // ─── Messages ───
    @Query("SELECT * FROM cached_messages WHERE sessionId = :sid ORDER BY ts ASC")
    fun observeMessages(sid: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM cached_messages WHERE sessionId = :sid ORDER BY ts ASC")
    suspend fun getMessages(sid: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM cached_messages WHERE sessionId = :sid")
    suspend fun clearMessages(sid: Int)

    @Query("DELETE FROM cached_messages")
    suspend fun clearAllMessages()
}
