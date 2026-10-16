package com.gptplus18.app.ui.screens.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gptplus18.app.ui.components.ScrollToBottomFab
import com.gptplus18.app.ui.theme.AppHaptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatMessagesList — LazyColumn & FAB
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.13
 * ═══════════════════════════════════════════════════════════════
 *
 *  قائمة الرسائل + FAB للسكرول للأسفل.
 *  تُستدعى داخل Box من ChatScreen (Box يحتوي pointerInput للتحديد).
 *
 *  ⚠️ internal — تُستخدم من ChatScreen فقط
 */

@Composable
internal fun androidx.compose.foundation.layout.BoxScope.ChatMessagesList(
    state: ChatUiState,
    vm: ChatViewModel,
    listState: LazyListState,
    scope: CoroutineScope,
    haptics: AppHaptics,
    ctx: Context,
    clip: ClipboardManager,
    selectionEpoch: Int,
    bottomPad: Dp,
    showSessionsList: Boolean,
    isScrolledUp: Boolean,
    onFullscreenImage: (String) -> Unit,
    onLongPress: (com.gptplus18.app.data.models.Message) -> Unit,
    onEditInput: (String) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = bottomPad),
    ) {
        val visibleMessages = state.messages.filter { it.role != "thinking" && it.id != -2 }

        if (visibleMessages.isEmpty() && !state.isSending && !state.isUploading) {
            item { EmptyLogoState() }
        }

        itemsIndexed(visibleMessages, key = { _, it -> it.ts.toString() }) { index, msg ->
            key(selectionEpoch) {
                val _isTemp = (msg.id == -2)
                // ⭐ v19.0: Staggered entry — تأخير متزايد حتى 250ms
                val _staggerDelay = (index * 25).coerceAtMost(250)
                AnimatedVisibility(
                    visible = if (_isTemp) !state.isSending && msg.content.isNotBlank() else true,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = _staggerDelay,
                        ),
                    ) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = _staggerDelay,
                            easing = FastOutSlowInEasing,
                        ),
                    ),
                ) {
                    MessageBubble(
                        msg = msg,
                        onImageClick = onFullscreenImage,
                        onLongPress = { haptics.openDialog(); onLongPress(msg) },
                        onCopy = { text ->
                            haptics.copyCode()
                            clip.setText(AnnotatedString("\u200E" + text + "\u200F"))
                            Toast.makeText(ctx, "✅ تم النسخ", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = { m -> onEditInput(m.content) },
                        onChoiceClick = { choice -> vm.send(choice) },
                    )
                }
            }
        }

        if (shouldShowThinkingBox(state, showSessionsList)) {
            item {
                ChatThinkingBox(
                    state = state,
                    showSessionsList = showSessionsList,
                )
            }
        }

        if (state.codeFiles.isNotEmpty() || state.codeZipUrl != null) {
            item {
                CodeFilesCard(
                    files = state.codeFiles,
                    zipUrl = state.codeZipUrl,
                )
            }
        }
    }

    ScrollToBottomFab(
        visible = isScrolledUp,
        onClick = {
            scope.launch {
                val total = listState.layoutInfo.totalItemsCount
                if (total > 0) {
                    listState.animateScrollToItem(total - 1)
                }
            }
        },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 12.dp),
    )
}
