package com.gptplus18.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Summarize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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

// ═══ Gunmetal Composer Palette ═══
private val ComposerTop     = Color(0xFF1C1D1F)
private val ComposerBottom  = Color(0xFF141517)
private val ComposerBorder  = Color(0xFF2E3034)

private val BtnBg           = Color(0xFF1F2124)
private val BtnBgDark       = Color(0xFF151719)
private val BtnBorder       = Color(0xFF303338)
private val BtnIcon         = Color(0xFFA8AAB0)

private val SendIdle        = Color(0xFF25272B)
private val SendIdleBorder  = Color(0xFF2A2C30)
private val SendActiveBorder = Color(0xFF5A5E66)
private val SendIdleIcon    = Color(0xFF7A7C82)
private val SendActiveIcon  = Color(0xFFF0F0F2)

private val SendGray = Color(0xFF3A3A3E)  // legacy (احتياط)

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
                    elevation = 20.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = Color.Black.copy(alpha = 0.65f),
                    spotColor = Color.Black.copy(alpha = 0.5f),
                )
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.verticalGradient(listOf(ComposerTop, ComposerBottom)))
                .border(0.5.dp, ComposerBorder, RoundedCornerShape(30.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                        color = Color(0xFF5A5C62),
                        fontSize = 13.2.sp,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = true,
                    textStyle = TextStyle(
                        color = Color(0xFFE5E5E7),
                        fontSize = 13.2.sp,
                        lineHeight = 21.sp,
                    ),
                    cursorBrush = SolidColor(Color(0xFFB0B2B8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp, max = 160.dp),
                    maxLines = 8,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 😊 إيموجي
                MetalButton(
                    onClick = { showEmojiSheet = true },
                    enabled = enabled,
                ) {
                    Icon(
                        Icons.Default.EmojiEmotions,
                        stringResource(R.string.t_230),
                        tint = BtnIcon,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.width(8.dp))

                // 🎤 مايك
                if (onVoiceInput != null) {
                    MetalButton(
                        onClick = {},
                        enabled = enabled,
                        noClick = true,
                    ) {
                        VoiceInputButton(
                            enabled = enabled,
                            onResult = { text -> onVoiceInput(text) },
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                // + إرفاق
                MetalButton(
                    onClick = onAttachClick,
                    enabled = enabled,
                ) {
                    Icon(
                        Icons.Default.Add,
                        stringResource(R.string.t_231),
                        tint = BtnIcon,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.weight(1f))

                // ← إرسال (دائري)
                if (hasContent) {
                    val btnEnabled = canSend && enabled
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(
                                elevation = if (btnEnabled) 8.dp else 0.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.5f),
                                spotColor = Color.Black.copy(alpha = 0.35f),
                            )
                            .clip(CircleShape)
                            .then(
                                if (btnEnabled) Modifier.background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF565A63), Color(0xFF3E4249), Color(0xFF2F3238))
                                    )
                                ) else Modifier.background(SendIdle)
                            )
                            .border(
                                0.5.dp,
                                if (btnEnabled) SendActiveBorder else SendIdleBorder,
                                CircleShape,
                            )
                            .clickable(enabled = btnEnabled, onClick = onSend),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isUploading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color(0xFFE5E5E7),
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                Icons.Default.ArrowUpward,
                                stringResource(R.string.t_035),
                                tint = if (btnEnabled) SendActiveIcon else SendIdleIcon,
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
                    fontSize = 15.8.sp,
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
                            Text(emoji, fontSize = 22.9.sp)
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
                Text(fileEmoji(attachment.fileName), fontSize = 28.2.sp)
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
                    color = Color(0xFFA8AAB0),
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(38.dp),
                )
                Text(
                    text = "${(attachment.progress * 100).toInt().coerceIn(0, 100)}%",
                    color = Color.White,
                    fontSize = 9.7.sp,
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
                        .background(Color(0xFFA8AAB0)),
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


// ═══════════════════════════════════════════════════════════
//  Gunmetal — Quick Actions + Small Chips
// ═══════════════════════════════════════════════════════════

private val ChipBg          = Color(0xFF1A1B1E)
private val ChipBorder      = Color(0xFF25272B)
private val ChipText        = Color(0xFFA0A2A8)
private val ChipIcon        = Color(0xFF8A8C92)


@Composable
private fun QuickChipsRow(
    enabled: Boolean,
    onTextInsert: (String) -> Unit,
    onImagePick: (() -> Unit)?,
    onEditImagePick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallChip(
            label = "صورة",
            icon = androidx.compose.material.icons.Icons.Outlined.AutoAwesome,
            onClick = { onTextInsert("أنشئ صورة ") },
            enabled = enabled,
        )
        SmallChip(
            label = "أغنية",
            icon = androidx.compose.material.icons.Icons.Outlined.MusicNote,
            onClick = { onTextInsert("أنشئ أغنية عن ") },
            enabled = enabled,
        )
        SmallChip(
            label = "فيديو",
            icon = androidx.compose.material.icons.Icons.Outlined.Movie,
            onClick = { onTextInsert("أنشئ فيديو عن ") },
            enabled = enabled,
        )
    }
}


@Composable
private fun SmallChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ChipBg)
            .border(0.5.dp, ChipBorder, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = ChipIcon,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            color = ChipText,
            fontSize = 9.7.sp,
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  MetalButton — زر معدني ثقيل صغير (32dp)
// ═══════════════════════════════════════════════════════════

@Composable
private fun MetalButton(
    onClick: () -> Unit,
    enabled: Boolean,
    noClick: Boolean = false,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "metalBtnScale",
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.25f),
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(BtnBg, BtnBgDark)))
            .border(0.5.dp, BtnBorder, RoundedCornerShape(10.dp))
            .then(
                if (!noClick) Modifier.clickable(enabled = enabled, onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
