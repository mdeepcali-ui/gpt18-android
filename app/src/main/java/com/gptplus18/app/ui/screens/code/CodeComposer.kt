package com.gptplus18.app.ui.screens.code

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.gptplus18.app.ui.components.ChatGptComposer
import com.gptplus18.app.ui.theme.AppHaptics

/**
 * ═══════════════════════════════════════════════════════════════
 *  CodeComposer — Wrapper around ChatGptComposer
 *  UXRD-2026-10-16-001 · Phase 4 · D-4.4
 * ═══════════════════════════════════════════════════════════════
 *
 *  غلاف ChatGptComposer لشاشة البرمجة — يحتوي منطق الإرسال
 *  والمرفقات للـ CodeViewModel.
 *
 *  ⚠️ internal — تُستخدم من CodeScreen فقط
 */

@Composable
internal fun CodeComposer(
    value: String,
    onValueChange: (String) -> Unit,
    state: CodeUiState,
    vm: CodeViewModel,
    keyboardController: SoftwareKeyboardController?,
    haptics: AppHaptics,
    onAttachRequest: () -> Unit,
) {
    ChatGptComposer(
        value = value,
        onValueChange = onValueChange,
        enabled = !state.isRunning || state.isRunning,
        attachments = state.pendingAttachments,
        onAttachClick = onAttachRequest,
        onSend = {
            haptics.sendMessage()
            val _hasAtt = state.pendingAttachments.isNotEmpty()
            if (value.isNotBlank() || _hasAtt) {
                keyboardController?.hide()
                if (_hasAtt) {
                    vm.submitWithAttachments(value)
                } else {
                    vm.submit(value)
                }
                onValueChange("")
            }
        },
        onRemoveAttachment = { id -> vm.removeAttachment(id) },
        isStreaming = state.isRunning,
        onStop = { vm.stopStreaming() },
        showMediaChips = false,
    )
}
