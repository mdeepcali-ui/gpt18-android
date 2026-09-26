package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gptplus18.app.R
import com.gptplus18.app.data.models.Attachment
import com.gptplus18.app.ui.theme.LocalAppColors

private val SendBlue = Color(0xFF0A84FF)  // زر الإرسال — أزرق
private val SendGray = Color(0xFF3A3A3E)  // زر الإرسال — رمادي أثناء الرفع

@Composable
fun ChatGptComposer(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    attachments: List<Attachment>,
    onAttachClick: () -> Unit,
    onSend: () -> Unit,
    onRemoveAttachment: (Long) -> Unit,
    onVoiceInput: ((String) -> Unit)? = null,
    onImagePick: (() -> Unit)? = null,      // ⭐ v2.0: لاختيار صورة
    onEditImagePick: (() -> Unit)? = null,  // ⭐ v2.0: لتعديل صورة
) {
    val hasText = value.isNotBlank()
    val hasAttachments = attachments.isNotEmpty()
    // ⭐ كل المرفقات مرفوعة 100؟
    val allUploaded = attachments.all { it.isUploaded }
    val hasContent = hasText || hasAttachments
    val isUploading = hasAttachments && !allUploaded
    val canSend = hasContent && allUploaded
    var showEmojiSheet by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.6f),
                )
                .clip(RoundedCornerShape(28.dp))
                .background(colors.composerBg)
                .border(0.5.dp, colors.composerBorder, RoundedCornerShape(28.dp))
                .padding(6.dp),
        ) {
            if (attachments.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(attachments, key = { it.id }) { att ->
                        AttachmentChip(
                            attachment = att,
                            onRemove = { onRemoveAttachment(att.id) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            // ⭐ v2.0: فقاعات سريعة
            QuickChipsRow(
                enabled = enabled,
                onTextInsert = { t -> onValueChange(value + t) },
                onImagePick = onImagePick,
                onEditImagePick = onEditImagePick,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.t_001),
                        color = colors.textTertiary,
                        fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    ),
                    cursorBrush = SolidColor(SendBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp, max = 160.dp),
                    maxLines = 8,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // إيموجي
                Icon(
                    Icons.Default.EmojiEmotions,
                    stringResource(R.string.t_230),
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(enabled = enabled) { showEmojiSheet = true },
                )
                Spacer(Modifier.width(6.dp))

                // 🎤 زر الإدخال الصوتي
                if (onVoiceInput != null) {
                    VoiceInputButton(
                        enabled = enabled,
                        onResult = { text -> onVoiceInput(text) },
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                } else {
                    Spacer(Modifier.width(4.dp))
                }

                // +
                Icon(
                    Icons.Default.Add,
                    stringResource(R.string.t_231),
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = enabled, onClick = onAttachClick),
                )

                Spacer(Modifier.weight(1f))

                if (hasContent) {
                    // ⭐ الزر يظهر دائماً عند وجود محتوى:
                    //    - رمادي أثناء الرفع (disabled)
                    //    - أزرق عند اكتمال الرفع (enabled)
                    val btnColor = if (canSend && enabled) SendBlue else SendGray
                    val btnEnabled = canSend && enabled
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(btnColor)
                            .clickable(enabled = btnEnabled, onClick = onSend),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isUploading) {
                            // ⭐ دائرة تحميل صغيرة بدل السهم
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                Icons.Default.ArrowUpward,
                                stringResource(R.string.t_035),
                                tint = if (btnEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEmojiSheet) {
        EmojiPickerSheet(
            onDismiss = { showEmojiSheet = false },
            onSelect = { emoji ->
                onValueChange(value + emoji)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiPickerSheet(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val colors = LocalAppColors.current

    CompositionLocalProvider(LocalAppColors provides colors) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = colors.surface,
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    stringResource(R.string.t_002),
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(COMMON_EMOJIS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(emoji) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(emoji, fontSize = 26.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private val COMMON_EMOJIS = listOf(
    "😀","😃","😄","😁","😅","😂","🤣","😊",
    "😇","🙂","🙃","😉","😌","😍","🥰","😘",
    "😗","😙","😚","😋","😛","😝","😜","🤪",
    "🤨","🧐","🤓","😎","🥳","😏","😒","😞",
    "😔","😟","😕","🙁","😣","😖","😫","😩",
    "🥺","😢","😭","😤","😠","😡","🤬","🤯",
    "😳","🥵","🥶","😱","😨","😰","😥","😓",
    "🤗","🤔","🤭","🤫","🤥","😶","😐","😑",
    "❤️","🧡","💛","💚","💙","💜","🖤","🤍",
    "👍","👎","👌","✌️","🤞","🤟","🤘","🤙",
    "👋","🤚","🖐️","✋","🖖","👏","🙌","🤝",
    "🙏","💪","🦾","✨","🔥","⭐","🌟","💫",
    "🎉","🎊","🎁","🎈","🎂","🍕","🍔","☕",
)

@Composable
private fun AttachmentChip(
    attachment: Attachment,
    onRemove: () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant),
    ) {
        if (attachment.isImage) {
            AsyncImage(
                model = attachment.uri,
                contentDescription = attachment.fileName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(fileEmoji(attachment.fileName), fontSize = 32.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(colors.textSecondary.copy(alpha = 0.7f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Close,
                stringResource(R.string.t_046),
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }

        if (attachment.isUploading) {
            // 🌑 طبقة تعتيم خلف المؤشر
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                // 🎯 دائرة progress + نسبة مئوية
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { attachment.progress.coerceIn(0f, 1f) },
                    color = SendBlue,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(38.dp),
                )
                Text(
                    text = "${(attachment.progress * 100).toInt().coerceIn(0, 100)}%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            // 📊 شريط progress سفلي واضح
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color.Black.copy(alpha = 0.4f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(attachment.progress.coerceIn(0f, 1f))
                        .background(SendBlue),
                )
            }
        }
    }
}

private fun fileEmoji(name: String): String {
    val n = name.lowercase()
    return when {
        n.endsWith(".pdf") -> "📄"
        n.endsWith(".doc") || n.endsWith(".docx") -> "📝"
        n.endsWith(".xls") || n.endsWith(".xlsx") -> "📊"
        n.endsWith(".zip") || n.endsWith(".rar") -> "🗜️"
        n.endsWith(".apk") -> "📦"
        n.endsWith(".mp3") || n.endsWith(".wav") -> "🎵"
        n.endsWith(".mp4") || n.endsWith(".mov") -> "🎥"
        else -> "📎"
    }
}


// ═══════════════════════════════════════════════════════
// ⭐ v2.0: QuickChipsRow — فقاعات سريعة فوق الـ input
// ═══════════════════════════════════════════════════════
@Composable
private fun QuickChipsRow(
    enabled: Boolean,
    onTextInsert: (String) -> Unit,
    onImagePick: (() -> Unit)?,
    onEditImagePick: (() -> Unit)?,
) {
    val colors = LocalAppColors.current
    val chips = listOf(
        Triple("بحث", "🔍", "ابحث عن "),
        Triple("صورة", "📷", null),
        Triple("أنشئ صورة", "🎨", "أنشئ صورة "),
        Triple("تعديل صورة", "✏️", null),
        Triple("فيديو", "🎬", "أنشئ فيديو عن "),
        Triple("أغنية", "🎵", "أنشئ أغنية عن "),
        Triple("كود", "💻", "اكتب كود "),
        Triple("لخّص", "📝", "لخّص "),
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        userScrollEnabled = true,
    ) {
        items(chips) { (label, emoji, insertText) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.bgSecondary.copy(alpha = 0.6f))
                    .border(
                        width = 0.5.dp,
                        color = colors.textSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .clickable(enabled = enabled) {
                        when {
                            insertText != null -> onTextInsert(insertText)
                            label == "صورة" -> onImagePick?.invoke()
                            label == "تعديل صورة" -> onEditImagePick?.invoke()
                        }
                    }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        label,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
