package com.gptplus18.app.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_sessions")
data class SessionEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val createdAt: Double,
    val updatedAt: Double,
    val msgCount: Int,
    val lastMsg: String?,
)

@Entity(tableName = "cached_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val id: Int,
    val sessionId: Int,
    val role: String,
    val content: String,
    val ts: Double,
)
