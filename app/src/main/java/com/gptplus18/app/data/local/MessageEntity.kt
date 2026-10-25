package com.gptplus18.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val sessionId: String,
    val role: String, // "user" أو "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
