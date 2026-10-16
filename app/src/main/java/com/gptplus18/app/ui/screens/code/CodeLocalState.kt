package com.gptplus18.app.ui.screens.code

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gptplus18.app.data.models.CodeSession

/**
 * ═══════════════════════════════════════════════════════════════
 *  CodeLocalState — UI-Only State Container
 *  UXRD-2026-10-16-001 · Phase 4 · D-4.7
 * ═══════════════════════════════════════════════════════════════
 *
 *  يحتوي كل الـ UI state المؤقت في CodeScreen:
 *    • input          — نص الإدخال
 *    • overlays       — حوارات مفتوحة
 *    • camera         — حالة الكاميرا
 *    • selectionEpoch — لإعادة تشكيل الرسائل
 *
 *  ⚠️ منفصل عن CodeUiState (الموجود في CodeViewModel).
 */

internal class CodeLocalState {
    // ── الإدخال ──
    var input by mutableStateOf("")

    // ── الحوارات ──
    var showAttachSheet by mutableStateOf(false)
    var showRunDialog by mutableStateOf(false)
    var deleteDialogFor by mutableStateOf<CodeSession?>(null)
    var fullscreenImage by mutableStateOf<String?>(null)

    // ── الكاميرا ──
    var cameraUri by mutableStateOf<Uri?>(null)

    // ── التحديد ──
    var selectionEpoch by mutableIntStateOf(0)
}

@Composable
internal fun rememberCodeLocalState(): CodeLocalState =
    remember { CodeLocalState() }
