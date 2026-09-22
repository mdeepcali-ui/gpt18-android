package com.gptplus18.app.util

import com.gptplus18.app.data.models.ChatMode

/**
 * Auto-Router — يقترح النموذج/الوضع حسب نص الطلب
 */
object AutoRouter {

    fun suggestMode(text: String): ChatMode {
        val t = text.lowercase()

        // كلمات مفتاحية للبرمجة
        val codeKeywords = listOf(
            "كود", "برمجة", "برمج", "code", "function", "دالة",
            "python", "kotlin", "javascript", "java", "html", "css",
            "api", "database", "sql", "bug", "خطأ", "fix",
            "طبق", "تطبيق", "موقع", "مشروع", "برنامج",
        )

        // كلمات مفتاحية للوسائط
        val mediaKeywords = listOf(
            "صورة", "صور", "ارسم", "رسم", "أغنية", "اغنية", "موسيقى",
            "فيديو", "video", "image", "picture", "song", "music",
            "ولد", "أنشئ", "اعمللي", "عملي",
        )

        val hasCode = codeKeywords.any { t.contains(it) }
        val hasMedia = mediaKeywords.any { t.contains(it) }

        return when {
            hasCode && hasMedia -> ChatMode.MAX
            hasCode -> ChatMode.CODE
            hasMedia -> ChatMode.MEDIA
            else -> ChatMode.CHAT
        }
    }

    /**
     * يقترح نموذج Code Agent حسب صعوبة الطلب
     */
    fun suggestCodeModel(text: String): String {
        val t = text.lowercase()
        val complexKeywords = listOf(
            "مشروع", "تطبيق كامل", "نظام", "backend", "frontend",
            "قاعدة بيانات", "database", "authentication", "مصادقة",
        )
        val simpleKeywords = listOf(
            "دالة", "function", "سطر", "بسيط", "simple",
        )

        return when {
            complexKeywords.any { t.contains(it) } -> "auto" // فريق كامل
            simpleKeywords.any { t.contains(it) } -> "qwen/qwen3-coder-plus"
            else -> "auto"
        }
    }
}
