package com.gptplus18.app.data.models

import androidx.compose.ui.graphics.Color

enum class ChatMode(
    val key: String,
    val label: String,
    val description: String,
    val emoji: String,
    val color: Color,
    val requiresSub: Boolean,
) {
    CHAT(
        key = "chat",
        label = "Chat",
        description = "نص + فهم صور",
        emoji = "💬",
        color = Color(0xFF7EE787),  // أخضر
        requiresSub = false,
    ),
    CODE(
        key = "code",
        label = "Code",
        description = "كتابة كود",
        emoji = "💻",
        color = Color(0xFF93E0FF),  // أزرق
        requiresSub = true,
    ),
    MEDIA(
        key = "media",
        label = "Media",
        description = "صور + أغاني",
        emoji = "🎬",
        color = Color(0xFFFFC58F),  // برتقالي
        requiresSub = true,
    ),
    MAX(
        key = "max",
        label = "Max",
        description = "كل شي — نص + وسائط",
        emoji = "⚡",
        color = Color(0xFFC4B5FD),  // بنفسجي
        requiresSub = true,
    ),
}
