package com.gptplus18.app.ui.screens.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatThinkingBox — Thinking Indicator Component
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.12
 * ═══════════════════════════════════════════════════════════════
 *
 *  صندوق التفكير في الشات — يستخرج:
 *    • النص الجاري من آخر رسالة (id == -2)
 *    • الحالة (status label)
 *    • منطق الظهور (isSending && !showSessionsList)
 *
 *  يعتمد على ThinkingShimmer المشترك (نفس الشكل في Code).
 *
 *  ⚠️ internal — تُستخدم من ChatScreen فقط
 */

/**
 * هل يجب عرض صندوق التفكير؟
 */
internal fun shouldShowThinkingBox(
    state: ChatUiState,
    showSessionsList: Boolean,
): Boolean = state.isSending && !showSessionsList

@Composable
internal fun ChatThinkingBox(
    state: ChatUiState,
    showSessionsList: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!shouldShowThinkingBox(state, showSessionsList)) return

    val thinkingText = state.messages
        .lastOrNull { it.id == -2 }
        ?.content
        ?.trim()
        .orEmpty()

    com.gptplus18.app.ui.components.ThinkingShimmer(
        text = thinkingText,
        status = state.statusLabel,
        modifier = modifier,
    )
}
