package com.gptplus18.app.ui.screens.chat

import android.net.Uri
import com.gptplus18.app.data.models.AllSession
import com.gptplus18.app.data.models.Session

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatHelpers — Utility Functions
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.1
 * ═══════════════════════════════════════════════════════════════
 *
 *  دوال مساعدة مستخرجة من ChatScreen.kt الأصلي:
 *    • toChatSession() — تحويل AllSession → Session
 *    • getFileSize()   — حساب حجم ملف من Uri
 */

/**
 * تحويل AllSession (من قائمة المحادثات) إلى Session (للاستخدام في الشات).
 * يُستخدم عند فتح محادثة من Drawer أو قائمة المحادثات.
 */
internal fun AllSession.toChatSession(): Session = Session(
    id = this.id,
    title = this.title,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    msgCount = this.msgCount,
    lastMsg = this.lastMsg,
)

/**
 * حساب حجم ملف من content:// Uri.
 * يُستخدم عند إرفاق صورة/ملف من الجهاز لحساب الحجم قبل الإرسال.
 *
 * @return الحجم بالبايت، أو 0 عند الفشل
 */
internal fun getFileSize(ctx: android.content.Context, uri: Uri): Long {
    return try {
        ctx.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
    } catch (_: Exception) {
        0L
    }
}
