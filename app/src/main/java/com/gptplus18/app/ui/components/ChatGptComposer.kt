package com.gptplus18.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gptplus18.app.R
import com.gptplus18.app.data.models.Attachment
import com.gptplus18.app.ui.theme.LocalAppColors
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.gptplus18.app.ui.theme.NotoNaskhFontFamily

// ═══ الألوان الأساسية الجديدة ═══
private val AccentBlue = Color(0xFF4B7EFF)
private val StopRed    = Color(0xFFFF453A)

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
    onImagePick: (() -> Unit)? = null,
    onEditImagePick: (() -> Unit)? = null,
    isStreaming: Boolean = false,
    onStop: (() -> Unit)? = null,
    showMediaChips: Boolean = true,
) {
    val colors = LocalAppColors.current
    val isLight = colors.isLight

    val hasText = value.isNotBlank()
    val hasAttachments = attachments.isNotEmpty()
    val allUploaded = attachments.all { it.isUploaded }
    val hasContent = hasText || hasAttachments
    val isUploading = hasAttachments && !allUploaded
    val canSend = hasContent && allUploaded

    // v5.1.8 - TextFieldValue داخلي + Custom Toolbar
    var tfv by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    val tfvRef = remember { mutableStateOf(tfv) }
    tfvRef.value = tfv

    LaunchedEffect(value) {
        if (value != tfv.text) tfv = TextFieldValue(value, TextRange(value.length))
    }

    LaunchedEffect(tfv.selection) {
        val sel = tfv.selection
        val txt = tfv.text
        if (sel.collapsed || sel.start >= sel.end || sel.end > txt.length) return@LaunchedEffect
        val selText = txt.substring(sel.start, sel.end)
        if (selText.contains(' ') || selText.contains('\n')) return@LaunchedEffect
        val urlRegex = Regex("https?://[^\\s\\u200E\\u200F]+")
        for (m in urlRegex.findAll(txt)) {
            if (sel.start >= m.range.first && sel.end <= m.range.last + 1 && selText.length < m.value.length) {
                tfv = tfv.copy(selection = TextRange(m.range.first, m.range.last + 1))
                break
            }
        }
    }

    val _ctx = androidx.compose.ui.platform.LocalContext.current
    val customToolbar = remember {
        CustomTextToolbar(
            onOpenBrowser = {
                extractUrlAt(tfvRef.value.text, tfvRef.value.selection)?.let { url ->
                    runCatching { _ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                }
            },
            canOpenBrowser = { extractUrlAt(tfvRef.value.text, tfvRef.value.selection) != null }
        )
    }
    customToolbar.Render()

    var showEmojiSheet by remember { mutableStateOf(false) }

    // ألوان ديناميكية حسب الوضع
    val boxBg     = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)  // ⭐ v18.9
    val boxBorder = if (isLight) Color(0xFFE5E5EA) else Color(0xFF3D3D3D)  // ⭐ v18.9
    val textColor = if (isLight) Color(0xFF1A1A1C) else Color(0xFFE5E5E7)
    val hintColor = if (isLight) Color(0xFF8E8E93) else Color(0xFF5A5C62)
    val iconColor = if (isLight) Color(0xFF1A1A1C) else Color(0xFFE5E5E7)
    val sendIdleBg = if (isLight) Color(0xFFE5E5EA) else Color(0xFF25272B)
    val sendIdleFg = if (isLight) Color(0xFF8E8E93) else Color(0xFF7A7C82)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
    ) {
        // ═══ Chips خارج الصندوق (فقط في الشات) ═══
        if (showMediaChips) {
            QuickChipsRow(
                enabled = enabled,
                onTextInsert = { t -> onValueChange(t) },
                isLight = isLight,
            )
            Spacer(Modifier.height(10.dp))
        }

        // ═══ الصندوق الرئيسي ═══
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(boxBg)
                .border(1.6.dp, Brush.linearGradient(listOf(Color(0xFF3D2F22), Color(0xFF5A4938), Color(0xFF3D2F22))), RoundedCornerShape(28.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            // مرفقات
            if (attachments.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(attachments, key = { it.id }) { att ->
                        AttachmentChip(
                            attachment = att,
                            onRemove = { onRemoveAttachment(att.id) },
                        )
                    }
                }
            }

            // حقل النص
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.t_001),
                        color = hintColor,
                        fontSize = 13.5.sp,
                    )
                }
                CompositionLocalProvider(LocalTextToolbar provides customToolbar) {
                    BasicTextField(
                        value = tfv,
                        onValueChange = { new ->
                            tfv = new
                            onValueChange(new.text)
                        },
                        enabled = true,
                        textStyle = TextStyle(
                            fontFamily = NotoNaskhFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 13.5.sp,
                            lineHeight = 17.sp,
                        ),
                        cursorBrush = SolidColor(AccentBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 24.dp, max = 160.dp),
                        maxLines = 8,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ═══ صف الأزرار ═══
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconOnlyButton(
                    icon = Icons.Default.Add,
                    contentDesc = stringResource(R.string.t_231),
                    onClick = onAttachClick,
                    enabled = enabled,
                    tint = iconColor,
                )
                Spacer(Modifier.width(16.dp))

                if (onVoiceInput != null) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        VoiceInputButton(
                            enabled = enabled,
                            onResult = { text -> onVoiceInput(text) },
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                }

                IconOnlyButton(
                    icon = Icons.Default.EmojiEmotions,
                    contentDesc = stringResource(R.string.t_230),
                    onClick = { showEmojiSheet = true },
                    enabled = enabled,
                    tint = iconColor,
                )

                Spacer(Modifier.weight(1f))

                if (isStreaming && onStop != null) {
                    ActionCircleButton(
                        icon = Icons.Default.Stop,
                        contentDesc = "إيقاف",
                        onClick = onStop,
                        enabled = true,
                        bgColor = AccentBlue,
                        iconColor = Color.White,
                        size = 30.dp,
                        iconSize = 18.dp,
                    )
                } else if (hasContent) {
                    ActionCircleButton(
                        icon = Icons.Default.ArrowUpward,
                        contentDesc = stringResource(R.string.t_035),
                        onClick = onSend,
                        enabled = canSend && enabled,
                        bgColor = if (canSend && enabled) AccentBlue else sendIdleBg,
                        iconColor = if (canSend && enabled) Color.White else sendIdleFg,
                        size = 30.dp,
                        iconSize = 18.dp,
                        isUploading = isUploading,
                    )
                }
            }
        }
    }

    if (showEmojiSheet) {
        EmojiPickerSheet(
            onDismiss = { showEmojiSheet = false },
            onSelect = { emoji -> onValueChange(value + emoji) },
        )
    }
}

// ═══ زر أيقونة شفاف ═══
@Composable
private fun IconOnlyButton(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit,
    enabled: Boolean,
    tint: Color,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "iconBtnScale",
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(enabled = enabled) {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp),
        )
    }
}

// ═══ زر دائري ملوّن (إرسال/إيقاف) ═══
@Composable
private fun ActionCircleButton(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit,
    enabled: Boolean,
    bgColor: Color,
    iconColor: Color,
    size: Dp,
    iconSize: Dp,
    isUploading: Boolean = false,
) {
    val _interaction = androidx.compose.runtime.remember { MutableInteractionSource() }
    val _pressed by _interaction.collectIsPressedAsState()
    val _scale by animateFloatAsState(
        targetValue = if (_pressed && enabled) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "send_btn_scale",
    )
    // ⭐ v19.0: Icon Launch — السهم "ينطلق" للأعلى عند الضغط
    val _iconLift by animateFloatAsState(
        targetValue = if (_pressed && enabled) -3f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "send_icon_lift",
    )
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = _scale; scaleY = _scale }
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = _interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isUploading) {
            CircularProgressIndicator(
                color = iconColor,
                modifier = Modifier.size(iconSize),
                strokeWidth = 2.dp,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer { translationY = _iconLift },
                contentAlignment = Alignment.Center,
            ) {
                listOf(
                    Pair(-1f, -1f), Pair(1f, -1f),
                    Pair(-1f, 1f), Pair(1f, 1f),
                    Pair(0f, 0f),
                ).forEach { (dx, dy) ->
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDesc,
                        tint = iconColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = dx
                                translationY = dy
                            },
                    )
                }
            }
        }
    }
}

// ═══ صف الـ chips (خارج الصندوق) ═══
@Composable
private fun QuickChipsRow(
    enabled: Boolean,
    onTextInsert: (String) -> Unit,
    isLight: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallChip(
            label = "صورة",
            icon = Icons.Outlined.AutoAwesome,
            onClick = { onTextInsert("أنشئ صورة ") },
            enabled = enabled,
            isLight = isLight,
        )
        SmallChip(
            label = "أغنية",
            icon = Icons.Outlined.MusicNote,
            onClick = { onTextInsert("أنشئ أغنية عن ") },
            enabled = enabled,
            isLight = isLight,
        )
    }
}

@Composable
private fun SmallChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    isLight: Boolean,
) {
    val bg = if (isLight) Color(0xFFF0F0F2) else Color(0xFF0D0D0D)
    val fg = if (isLight) Color(0xFF6E6E73) else Color(0xFFA0A2A8)
    Row(
        modifier = Modifier
            .height(26.dp)
            .widthIn(min = 85.dp)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(
                1.2.dp,
                Brush.linearGradient(listOf(Color(0xFF3D2F22), Color(0xFF5A4938), Color(0xFF3D2F22))),
                RoundedCornerShape(50),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = fg,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            maxLines = 1,
            text = label,
            color = fg,
            fontSize = 11.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Medium,
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
                    fontSize = 15.sp,
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
                            Text(emoji, fontSize = 18.3.sp)
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
                Text(fileEmoji(attachment.fileName), fontSize = 22.6.sp)
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
                modifier = Modifier.size(12.dp),
            )
        }

        if (attachment.isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { attachment.progress.coerceIn(0f, 1f) },
                    color = Color(0xFFA8AAB0),
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

// v5.1.8 - استخراج رابط URL عند موقع التحديد
private fun extractUrlAt(text: String, sel: TextRange): String? {
    if (text.isEmpty()) return null
    val urlRegex = Regex("https?://[^\\s\\u200E\\u200F]+")
    for (m in urlRegex.findAll(text)) {
        val r = m.range
        if (sel.collapsed) {
            if (sel.start in r.first..(r.last + 1)) return m.value
        } else {
            if (sel.start >= r.first && sel.end <= r.last + 1) return m.value
        }
    }
    return null
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
