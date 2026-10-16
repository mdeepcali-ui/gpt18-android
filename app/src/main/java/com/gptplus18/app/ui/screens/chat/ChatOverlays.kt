package com.gptplus18.app.ui.screens.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.gptplus18.app.R
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import com.gptplus18.app.ui.components.AddonsSheet
import com.gptplus18.app.ui.components.AttachMenuSheet
import com.gptplus18.app.ui.components.DeepThinkSheet
import com.gptplus18.app.ui.components.FullscreenImageViewer
import com.gptplus18.app.ui.components.MessageActionsSheet
import com.gptplus18.app.ui.theme.AppHaptics
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatOverlays — Dialogs & Sheets
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.11
 * ═══════════════════════════════════════════════════════════════
 *
 *  كل الـ overlays في ChatScreen:
 *    • AttachMenuSheet · AddonsSheet · DeepThinkSheet
 *    • Delete AlertDialog · MessageActionsSheet
 *    • Subscription/Fullscreen overlays
 *
 *  ⚠️ internal — تُستخدم من ChatScreen فقط
 */

@Composable
internal fun ChatOverlays(
    state: ChatUiState,
    vm: ChatViewModel,
    haptics: AppHaptics,
    ctx: Context,
    clip: ClipboardManager,
    showAttachSheet: Boolean,
    showAddonsSheet: Boolean,
    showDeepThinkSheet: Boolean,
    deleteDialogFor: Session?,
    actionsSheetFor: Message?,
    fullscreenImage: String?,
    input: String,
    onInputChange: (String) -> Unit,
    onDismissAttach: () -> Unit,
    onDismissAddons: () -> Unit,
    onDismissDeepThink: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissActions: () -> Unit,
    onDismissFullscreen: () -> Unit,
    onOpenAddons: () -> Unit,
    onOpenDeepThink: () -> Unit,
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
    onFiles: () -> Unit,
    onNavigateToSubscription: () -> Unit,
) {
    if (showAttachSheet) {
        AttachMenuSheet(
            onDismiss = onDismissAttach,
            onCamera = onCamera,
            onPhotos = onPhotos,
            onFiles = onFiles,
            onAddons = onOpenAddons,
            onDeepThink = onOpenDeepThink,
        )
    }

    if (showAddonsSheet) {
        AddonsSheet(
            onDismiss = onDismissAddons,
            onSelect = { text -> onInputChange(text) },
        )
    }

    if (showDeepThinkSheet) {
        DeepThinkSheet(
            onDismiss = onDismissDeepThink,
            onSelect = { text -> onInputChange(text) },
        )
    }

    deleteDialogFor?.let { s ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.t_045), color = TextPrimary) },
            text = { Text("متأكد بدك تحذف \"${s.title}\"؟", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { vm.deleteSession(s.id); onDismissDelete() }) {
                    Text(stringResource(R.string.t_046), color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.t_026), color = TextSecondary)
                }
            },
            containerColor = BgSecondary,
            shape = RoundedCornerShape(24.dp),
        )
    }

    actionsSheetFor?.let { msg ->
        MessageActionsSheet(
            msg = msg,
            isPinned = vm.isPinned(msg.ts),
            onPin = { vm.pinMessage(msg) },
            onCopy = {
                haptics.copyCode()
                clip.setText(AnnotatedString("\u200E" + msg.content + "\u200F"))
                Toast.makeText(ctx, "✅ تم النسخ", Toast.LENGTH_SHORT).show()
            },
            onShare = {
                val chooserTitle = ctx.getString(R.string.t_133)
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, msg.content)
                }
                ctx.startActivity(android.content.Intent.createChooser(intent, chooserTitle))
            },
            onReply = { vm.setReplyTo(msg) },
            onDelete = null,
            onDismiss = onDismissActions,
        )
    }

    state.subscriptionPrompt?.let { prompt ->
        SubscriptionRequiredModal(
            kind = prompt.kind,
            message = prompt.message,
            onSubscribe = {
                vm.dismissSubscriptionPrompt()
                onNavigateToSubscription()
            },
            onDismiss = { vm.dismissSubscriptionPrompt() },
        )
    }

    if (state.topupRequired) {
        TopupRequiredModal(
            action = state.topupAction ?: "",
            onTopup = {
                vm.clearTopup()
                onNavigateToSubscription()
            },
            onDismiss = { vm.clearTopup() },
        )
    }

    fullscreenImage?.let { url ->
        FullscreenImageViewer(imageUrl = url, onDismiss = onDismissFullscreen)
    }
}
