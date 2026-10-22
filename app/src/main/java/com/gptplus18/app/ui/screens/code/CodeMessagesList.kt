package com.gptplus18.app.ui.screens.code

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.ui.components.ScrollToBottomFab
import com.gptplus18.app.ui.theme.AppHaptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.gptplus18.app.ui.screens.chat.MessageBubble

/**
 * ═══════════════════════════════════════════════════════════════
 *  CodeMessagesList — LazyColumn & FAB
 *  UXRD-2026-10-16-001 · Phase 4 · D-4.3
 * ═══════════════════════════════════════════════════════════════
 *
 *  قائمة رسائل البرمجة + FAB للسكرول.
 *  يستخدم نفس MessageBubble + ThinkingShimmer المشتركة مع Chat.
 *
 *  ⚠️ internal — تُستخدم من CodeScreen فقط
 */

@Composable
internal fun BoxScope.CodeMessagesList(
    state: CodeUiState,
    listState: LazyListState,
    scope: CoroutineScope,
    haptics: AppHaptics,
    clip: ClipboardManager,
    selectionEpoch: Int,
    bottomPad: Dp,
    isScrolledUp: Boolean,
    onFullscreenImage: (String) -> Unit,
    onEditInput: (String) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = bottomPad),
    ) {
        items(state.messages, key = { it.id.toString() + it.ts }) { m ->
            if (m.id == -2) {
                Column(Modifier.fillMaxWidth()) {
                    // ⭐ v19.0: CodeThinkingBox (Phase 4)
                    CodeThinkingBox(state = state)
                    AnimatedVisibility(
                        visible = !state.isRunning && m.content.isNotBlank(),
                        enter = fadeIn(animationSpec = tween(280)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing),
                            ),
                    ) {
                        val asMsg = Message(
                            id = m.id, role = m.role, content = m.content, ts = m.ts,
                            localAttachments = m.localAttachments,
                            localImageUris = m.localImageUris,
                        )
                        MessageBubble(
                            msg = asMsg,
                            onImageClick = onFullscreenImage,
                            onLongPress = { },
                            onCopy = { text -> haptics.copyCode(); clip.setText(AnnotatedString(text)) },
                            onEdit = { msg -> onEditInput(msg.content) },
                        )
                    }
                }
            } else {
                val asMsg = Message(
                    id = m.id, role = m.role, content = m.content, ts = m.ts,
                    localAttachments = m.localAttachments,
                    localImageUris = m.localImageUris,
                )
                MessageBubble(
                    msg = asMsg,
                    onImageClick = onFullscreenImage,
                    onLongPress = { },
                    onCopy = { text -> haptics.copyCode(); clip.setText(AnnotatedString(text)) },
                    onEdit = { msg -> onEditInput(msg.content) },
                )
            }
        }
        if (!state.isRunning && (state.files.isNotEmpty() || state.zipUrl != null)) {
            item {
                FilesDownloadSection(files = state.files, zipUrl = state.zipUrl)
            }
        }
    }

    ScrollToBottomFab(
        visible = isScrolledUp,
        onClick = {
            scope.launch {
                val total = listState.layoutInfo.totalItemsCount
                if (total > 0) listState.animateScrollToItem(total - 1)
            }
        },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 12.dp),
    )
}
