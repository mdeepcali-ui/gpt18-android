package com.gptplus18.app.ui.screens.code

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * ═══════════════════════════════════════════════════════════════
 *  CodeThinkingBox — Thinking Indicator
 *  UXRD-2026-10-16-001 · Phase 4 · D-4.6
 * ═══════════════════════════════════════════════════════════════
 *
 *  صندوق التفكير في شاشة البرمجة — يعرض:
 *    • نص التفكير (thinking text)
 *    • نص الرد الجاري (reply in progress)
 *    • الحالة (status label)
 *
 *  يستخدم ThinkingShimmer المشترك (نفس الشكل في Chat).
 *
 *  ⚠️ internal — تُستخدم من CodeMessagesList فقط
 */

@Composable
internal fun CodeThinkingBox(
    state: CodeUiState,
    modifier: Modifier = Modifier,
) {
    if (!state.isRunning) return

    val _think = state.thinkingText.trim()
    val _reply = state.messages
        .lastOrNull { it.id == -2 }
        ?.content
        ?.trim()
        .orEmpty()

    val _st = when {
        _think.isNotBlank() && _reply.isNotBlank() -> "$_think\n\n$_reply"
        _think.isNotBlank() -> _think
        else -> _reply
    }

    com.gptplus18.app.ui.components.ThinkingShimmer(
        text = _st,
        status = state.statusLabel,
        modifier = modifier,
    )
}
