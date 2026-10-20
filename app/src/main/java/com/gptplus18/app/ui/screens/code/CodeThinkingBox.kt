package com.gptplus18.app.ui.screens.code

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    if (state.toolActivity.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            state.toolActivity.takeLast(3).forEach { tool ->
                val icon = when (tool.ok) {
                    null -> "⏳"
                    true -> "✅"
                    false -> "❌"
                }
                val bg = when (tool.ok) {
                    null -> Color(0xFF1A1A2E)
                    true -> Color(0xFF0F3D2E)
                    false -> Color(0xFF4A1A1A)
                }
                val fg = when (tool.ok) {
                    null -> Color(0xFFB0B0B0)
                    true -> Color(0xFF4ADE80)
                    false -> Color(0xFFF87171)
                }
                Text(
                    text = icon + " " + tool.name,
                    modifier = Modifier
                        .background(bg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = fg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
    com.gptplus18.app.ui.components.ThinkingShimmer(
        text = _st,
        status = state.statusLabel,
        modifier = modifier,
    )
}
