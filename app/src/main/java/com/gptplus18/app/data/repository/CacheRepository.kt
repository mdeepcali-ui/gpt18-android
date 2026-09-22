package com.gptplus18.app.data.repository

import com.gptplus18.app.data.local.room.CacheDao
import com.gptplus18.app.data.local.room.MessageEntity
import com.gptplus18.app.data.local.room.SessionEntity
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheRepository @Inject constructor(
    private val dao: CacheDao,
) {
    // ─── Sessions ───
    suspend fun saveSessions(sessions: List<Session>) {
        try {
            dao.clearSessions()
            dao.insertSessions(sessions.map { s ->
                SessionEntity(
                    id = s.id,
                    title = s.title,
                    createdAt = s.createdAt,
                    updatedAt = s.updatedAt,
                    msgCount = s.msgCount,
                    lastMsg = s.lastMsg,
                )
            })
        } catch (_: Exception) { }
    }

    suspend fun getSessions(): List<Session> {
        return try {
            dao.getSessions().map { e ->
                Session(
                    id = e.id,
                    title = e.title,
                    createdAt = e.createdAt,
                    updatedAt = e.updatedAt,
                    msgCount = e.msgCount,
                    lastMsg = e.lastMsg,
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun deleteSession(sid: Int) {
        try {
            dao.deleteSession(sid)
            dao.clearMessages(sid)
        } catch (_: Exception) { }
    }

    // ─── Messages ───
    suspend fun saveMessages(sid: Int, messages: List<Message>) {
        try {
            dao.clearMessages(sid)
            dao.insertMessages(messages.map { m ->
                MessageEntity(
                    id = m.id,
                    sessionId = sid,
                    role = m.role,
                    content = m.content,
                    ts = m.ts,
                )
            })
        } catch (_: Exception) { }
    }

    suspend fun getMessages(sid: Int): List<Message> {
        return try {
            dao.getMessages(sid).map { e ->
                Message(
                    id = e.id,
                    role = e.role,
                    content = e.content,
                    ts = e.ts,
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun clearAll() {
        try {
            dao.clearSessions()
            dao.clearAllMessages()
        } catch (_: Exception) { }
    }
}
