package com.gptplus18.app.ui.screens.chat

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatLocalState — UI-Only State Container
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.14
 * ═══════════════════════════════════════════════════════════════
 *
 *  يحتوي كل الـ UI state المؤقت في ChatScreen:
 *    • input        — نص الإدخال
 *    • overlays     — حوارات/أوراق مفتوحة
 *    • search       — حالة البحث
 *    • IME          — قياسات الكيبورد
 *
 *  يُنشأ عبر rememberChatLocalState() — Singleton لكل session.
 *
 *  ⚠️ منفصل عن ChatUiState (الموجود في ChatViewModel) الذي يحوي
 *     الـ logical state من الباك إند (messages, sessions, isSending).
 */

internal class ChatLocalState {
    // ── الإدخال ──
    var input by mutableStateOf("")

    // ── الحوارات/الأوراق ──
    var showAttachSheet by mutableStateOf(false)
    var showAddonsSheet by mutableStateOf(false)
    var showDeepThinkSheet by mutableStateOf(false)
    var showSessionsList by mutableStateOf(false)
    var showSearch by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    // ── Overlays ──
    var deleteDialogFor by mutableStateOf<Session?>(null)
    var actionsSheetFor by mutableStateOf<Message?>(null)
    var fullscreenImage by mutableStateOf<String?>(null)

    // ── الكاميرا ──
    var cameraUri by mutableStateOf<Uri?>(null)

    // ── التحديد (لإعادة تشكيل الرسائل) ──
    var selectionEpoch by mutableIntStateOf(0)

    // ── IME (الكيبورد) ──
    var lastImeChangeAt by mutableLongStateOf(0L)
    var prevMsgSize by mutableIntStateOf(0)
}

@Composable
internal fun rememberChatLocalState(): ChatLocalState =
    remember { ChatLocalState() }
