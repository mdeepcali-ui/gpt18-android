package com.gptplus18.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * ═══════════════════════════════════════════════════════════════
 *  AppHaptics — Haptic Feedback Helper
 *  UXRD-2026-10-16-001 · Phase 3 · D-3.1
 * ═══════════════════════════════════════════════════════════════
 *
 *  واجهة موحّدة للـ Haptic Feedback.
 *
 *  المصفوفة:
 *    sendMessage   → TextHandleMove    (خفيف جداً)
 *    openDialog    → LongPress         (متوسط)
 *    copyCode      → LongPress         (متوسط)
 *    saveProject   → LongPress         (قوي)
 *    error         → LongPress         (قوي)
 *    toggle        → TextHandleMove    (خفيف)
 *    select        → TextHandleMove    (خفيف)
 *
 *  الاستخدام:
 *    val haptics = rememberAppHaptics()
 *    haptics.sendMessage()
 *    haptics.error()
 */

/**
 * API مبسّط للـ Haptics — استخدم rememberAppHaptics()
 */
class AppHaptics internal constructor(
    private val perform: (HapticFeedbackType) -> Unit,
) {
    /** إرسال رسالة — نبضة خفيفة جداً */
    fun sendMessage() = perform(HapticFeedbackType.TextHandleMove)

    /** فتح dialog / sheet — نبضة متوسطة */
    fun openDialog() = perform(HapticFeedbackType.LongPress)

    /** نسخ كود أو نص — نبضة متوسطة */
    fun copyCode() = perform(HapticFeedbackType.LongPress)

    /** حفظ مشروع / نجاح — نبضة قوية */
    fun saveSuccess() = perform(HapticFeedbackType.LongPress)

    /** خطأ / رفض — نبضة قوية */
    fun error() = perform(HapticFeedbackType.LongPress)

    /** تفعيل/إلغاء toggle — نبضة خفيفة */
    fun toggle() = perform(HapticFeedbackType.TextHandleMove)

    /** تحديد عنصر — نبضة خفيفة */
    fun select() = perform(HapticFeedbackType.TextHandleMove)

    /** بدء سحب / swipe — نبضة خفيفة */
    fun swipe() = perform(HapticFeedbackType.TextHandleMove)
}

/**
 * إنشاء instance من AppHaptics — للاستخدام في @Composable
 *
 * الاستخدام:
 *   val haptics = rememberAppHaptics()
 *   haptics.sendMessage()
 */
@Composable
fun rememberAppHaptics(): AppHaptics {
    val haptic = LocalHapticFeedback.current
    return androidx.compose.runtime.remember(haptic) {
        AppHaptics { type -> haptic.performHapticFeedback(type) }
    }
}
