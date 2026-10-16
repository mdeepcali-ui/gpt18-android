package com.gptplus18.app.ui.screens.code

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R
import com.gptplus18.app.data.models.CodeSession
import com.gptplus18.app.ui.components.AttachMenuSheet
import com.gptplus18.app.ui.components.FullscreenImageViewer
import com.gptplus18.app.ui.screens.chat.components.RunDialog
import com.gptplus18.app.ui.screens.chat.components.SaveDialog
import com.gptplus18.app.ui.theme.AppHaptics
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary

/**
 * ═══════════════════════════════════════════════════════════════
 *  CodeOverlays — Dialogs & Sheets
 *  UXRD-2026-10-16-001 · Phase 4 · D-4.5
 * ═══════════════════════════════════════════════════════════════
 *
 *  كل الـ overlays في CodeScreen:
 *    • AttachMenuSheet
 *    • AlertDialog (delete)
 *    • FullscreenImageViewer
 *    • SaveDialog
 *    • RunDialog + LaunchedEffect (output → input)
 *
 *  ⚠️ internal — تُستخدم من CodeScreen فقط
 */

@Composable
internal fun CodeOverlays(
    state: CodeUiState,
    vm: CodeViewModel,
    haptics: AppHaptics,
    showAttachSheet: Boolean,
    deleteDialogFor: CodeSession?,
    fullscreenImage: String?,
    showRunDialog: Boolean,
    onDismissAttach: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissFullscreen: () -> Unit,
    onDismissRun: () -> Unit,
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
    onFiles: () -> Unit,
    onEditInput: (String) -> Unit,
) {
    // ⭐ v5.0: قائمة المرفقات
    if (showAttachSheet) {
        AttachMenuSheet(
            onDismiss = onDismissAttach,
            onCamera = { onDismissAttach(); onCamera() },
            onPhotos = { onDismissAttach(); onPhotos() },
            onFiles = { onDismissAttach(); onFiles() },
            onAddons = onDismissAttach,
            onDeepThink = onDismissAttach,
        )
    }

    deleteDialogFor?.let { s ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.t_052), color = TextPrimary) },
            text = { Text("حذف \"${s.title}\"؟", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSession(s.id)
                    onDismissDelete()
                }) { Text(stringResource(R.string.t_046), color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.t_026), color = TextSecondary)
                }
            },
            containerColor = BgSecondary,
        )
    }

    // X2: viewer
    fullscreenImage?.let { url ->
        FullscreenImageViewer(
            imageUrl = url,
            onDismiss = onDismissFullscreen,
        )
    }

    // ⭐ v10.4: Save Dialog
    if (state.showSaveDialog) {
        SaveDialog(
            onDismiss = { vm.hideSaveDialog() },
            onSave = { name -> haptics.saveSuccess(); vm.manualSaveWorkspace(name) },
            isSaving = state.manualSaveLoading,
            lastError = state.manualSaveError,
        )
    }

    // ⭐ v8.0: Run Popup
    if (showRunDialog) {
        RunDialog(
            onDismiss = onDismissRun,
            isRunning = state.runLoading,
            lastError = state.runError,
            onExecute = { code, lang -> vm.runCode(code, lang) },
        )

        LaunchedEffect(state.runOutput, state.runError) {
            if (!state.runLoading &&
                (state.runOutput.isNotBlank() || state.runError.isNotBlank())
            ) {
                val boxed = if (state.runError.isNotBlank())
                    "[خطأ] " + state.runError
                else
                    state.runOutput
                onEditInput(boxed)
                onDismissRun()
            }
        }
    }
}
