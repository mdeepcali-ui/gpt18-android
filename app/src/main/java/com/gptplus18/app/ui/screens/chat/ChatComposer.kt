package com.gptplus18.app.ui.screens.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.gptplus18.app.ui.components.ChatGptComposer
import com.gptplus18.app.ui.theme.AppHaptics

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatComposer — Wrapper around ChatGptComposer
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.10
 * ═══════════════════════════════════════════════════════════════
 *
 *  غلاف ChatComposer — يحتوي منطق الإرسال والمرفقات
 *  والكيبورد والـ Haptic. يعزل ChatScreen عن التفاصيل.
 *
 *  ⚠️ internal — تُستخدم من ChatScreen فقط
 */

@Composable
internal fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    state: ChatUiState,
    vm: ChatViewModel,
    keyboardController: SoftwareKeyboardController?,
    haptics: AppHaptics,
    onAttachRequest: () -> Unit,
    onImagePickRequest: () -> Unit,
    onEditImagePickRequest: () -> Unit,
) {
    ChatGptComposer(
        value = value,
        onValueChange = onValueChange,
        enabled = !state.isSending && !state.isUploading,
        attachments = state.pendingAttachments,
        onAttachClick = onAttachRequest,
        onSend = {
            haptics.sendMessage()
            if (state.pendingAttachments.isNotEmpty()) {
                vm.sendWithAttachments(value)
            } else if (value.isNotBlank()) {
                vm.send(value)
            }
            onValueChange("")
            keyboardController?.hide()
        },
        onRemoveAttachment = { vm.removeAttachment(it) },
        onVoiceInput = { text ->
            onValueChange(if (value.isBlank()) text else "$value $text")
        },
        onImagePick = onImagePickRequest,
        onEditImagePick = onEditImagePickRequest,
        isStreaming = state.isSending,
        onStop = { vm.stopStreaming() },
        showMediaChips = true,
    )
}
