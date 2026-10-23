package com.gptplus18.app.ui.components

/**
 * ═══════════════════════════════════════════════════════════════
 *  MessageBubble — فقاعة الرسالة (مشتركة بين chat و code)
 *  نُقلت من chat/ChatMessageBubble.kt
 * ═══════════════════════════════════════════════════════════════
 */


import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gptplus18.app.R
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.ui.components.MarkdownText
import com.gptplus18.app.ui.components.MessageTimestamp
import com.gptplus18.app.ui.components.TypingIndicator
import com.gptplus18.app.ui.components.AudioPlayerCard
import com.gptplus18.app.ui.components.VideoPlayerCard
import com.gptplus18.app.ui.helpers.MessageHelpers
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.BubbleBg
import com.gptplus18.app.ui.theme.BubbleBorder
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextTertiary
import com.gptplus18.app.ui.theme.Info
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.items
import com.gptplus18.app.ui.components.ActionButton
import com.gptplus18.app.ui.components.ReactionButton

@OptIn(ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun UserMessageText(text: String) {
    // ⭐ طي الرسائل الطويلة — أكثر من 400 حرف أو 6 أسطر
    val CHAR_LIMIT = 400
    val LINE_LIMIT = 6
    val PREVIEW_LINES = 4
    val lineCount = text.count { it == '\n' } + 1
    val shouldCollapse = text.length > CHAR_LIMIT || lineCount > LINE_LIMIT
    var expanded by remember { mutableStateOf(false) }

    if (!shouldCollapse) {
        SelectionContainer {
            Text(
                text,
                color = TextPrimary,
                fontSize = 11.2.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
            )
        }
        return
    }

    Column {
        SelectionContainer {
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 11.2.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                maxLines = if (expanded) Int.MAX_VALUE else PREVIEW_LINES,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (expanded) "▲ عرض أقل" else "▼ عرض المزيد (${text.length} حرف)",
                color = Accent,
                fontSize = 9.1.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}



@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@Composable
fun MessageBubble(
    msg: Message,
    onImageClick: (String) -> Unit,
    onLongPress: () -> Unit,
    onCopy: (String) -> Unit,
    onEdit: (Message) -> Unit,
    onChoiceClick: (String) -> Unit = {},
) {
    val isUser = msg.role == "user"
    val imageUrl = MessageHelpers.extractImageUrl(msg.content)
    val audioUrl = MessageHelpers.extractAudioUrl(msg.content)
    val videoUrl = MessageHelpers.extractVideoUrl(msg.content)
    val cleanText = MessageHelpers.stripMediaMarkers(msg.content)
    // ⭐ محلي: صورة مرفوعة لم تُرفع للسيرفر بعد
    val localImage = msg.localImageUri
    val localImages: List<String> = msg.localImageUris ?: (localImage?.let { listOf(it) } ?: emptyList())
    val ctx = LocalContext.current
    // ⭐ v19.0: Copy Success Flash — وميض أخضر بعد النسخ
    var _copiedFlash by remember { androidx.compose.runtime.mutableStateOf(false) }
    val _flashAlpha by animateFloatAsState(
        targetValue = if (_copiedFlash) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = if (_copiedFlash) 120 else 450,
        ),
        label = "copy_flash",
    )
    LaunchedEffect(_copiedFlash) {
        if (_copiedFlash) {
            kotlinx.coroutines.delay(180)
            _copiedFlash = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = if (isUser) Modifier.widthIn(max = 320.dp) else Modifier.fillMaxWidth(),
        ) {
            if (isUser) {
                val _bInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val _bPressed by _bInteraction.collectIsPressedAsState()
                val _bScale by animateFloatAsState(
                    targetValue = if (_bPressed) 0.97f else 1f,
                    animationSpec = spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = 600f,
                    ),
                    label = "bubble_press",
                )
                // ⭐ v19.0: Border color يتغير مع Flash (أخضر عند النسخ)
                val _flashGreen = androidx.compose.ui.graphics.Color(0xFF5EBF7A)
                val _borderColor = androidx.compose.ui.graphics.lerp(
                    BubbleBorder,
                    _flashGreen,
                    _flashAlpha,
                )
                Surface(
                    color = BubbleBg,
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 0.5.dp + (1.0.dp * _flashAlpha),
                        color = _borderColor,
                    ),
                    shadowElevation = 4.dp + (2.dp * _flashAlpha),
                    modifier = Modifier
                        .graphicsLayer { scaleX = _bScale; scaleY = _bScale }
                        .combinedClickable(
                            interactionSource = _bInteraction,
                            indication = null,
                            onClick = { },
                            onLongClick = onLongPress,
                        ),
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        // ⭐ صور محلية (مرفوعة من الجهاز) — عرض كل الصور
                        if (localImages.isNotEmpty() && imageUrl == null) {
                            if (localImages.size == 1) {
                                AsyncImage(
                                    model = localImages[0],
                                    contentDescription = stringResource(R.string.t_135),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .widthIn(max = 220.dp)
                                        .heightIn(max = 260.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                )
                            } else {
                                // شبكة الصور — كل صورة بعرض 100dp
                                androidx.compose.foundation.layout.FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    maxItemsInEachRow = 2,
                                    modifier = Modifier.widthIn(max = 220.dp),
                                ) {
                                    localImages.forEach { uri ->
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = stringResource(R.string.t_135),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                        )
                                    }
                                }
                            }
                            if (cleanText.isNotBlank() || msg.localAttachments?.any { !it.isImage } == true) Spacer(Modifier.height(8.dp))
                        }
                        // ⭐ v9.1: عرض الملفات (غير الصور) كمربعات
                        msg.localAttachments?.filter { !it.isImage }?.forEach { att ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(
                                        color = androidx.compose.ui.graphics.Color(0xFF1E1E28),
                                        shape = RoundedCornerShape(10.dp),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFFC4B5FD),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = att.name,
                                    color = androidx.compose.ui.graphics.Color(0xFFE4E4E7),
                                    fontSize = 9.6.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        // ⭐ صورة من السيرفر
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = stringResource(R.string.t_135),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .combinedClickable(
                                        onClick = { onImageClick(imageUrl) },
                                        onLongClick = onLongPress,
                                    ),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 4.dp),
                            ) {
                                ActionButton(
                                    icon = Icons.Default.Add,
                                    label = "حفظ",
                                    onClick = {
                                        com.gptplus18.app.util.MediaShareHelper.saveToGallery(ctx, imageUrl, "image") { ok ->
                                            Toast.makeText(ctx, if (ok) "✅ تم حفظ الصورة بنجاح" else "❌ فشل الحفظ", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                )
                                ActionButton(
                                    icon = Icons.Default.Add,
                                    label = "مشاركة",
                                    onClick = {
                                        kotlinx.coroutines.GlobalScope.launch {
                                            com.gptplus18.app.util.MediaShareHelper.shareMedia(ctx, imageUrl, "image")
                                        }
                                    },
                                )
                            }
                            if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                        }
                        if (cleanText.isNotBlank()) {
                            // ✨ streaming: فضي ناعم — بعد الاكتمال: أبيض عادي
                            val isStreaming = (msg.id == -2)
                            if (isStreaming) {
                                Text(
                                    cleanText,
                                    color = androidx.compose.ui.graphics.Color(0xFFB8B8C2),
                                    fontSize = 9.6.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 17.sp,
                                )
                            } else {
                                // ⭐ v3.2: طي الرسائل الطويلة للمستخدم
                                UserMessageText(cleanText)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { },
                            onLongClick = onLongPress,
                        ),
                ) {
                    // ⭐ مؤشر جاري التحليل
                    if (msg.isAnalyzing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BubbleBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Accent,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "🔍 جاري تحليل الصورة...",
                                color = TextPrimary,
                                fontSize = 9.8.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = stringResource(R.string.t_135),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { onImageClick(imageUrl) },
                                    onLongClick = onLongPress,
                                ),
                        )
                                                // ⭐ فقاعات إجراءات الصورة (تحويل / تعديل / حفظ / مشاركة)
                        val _imgCtx = LocalContext.current
                        val _imgScope = rememberCoroutineScope()
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val imgActions = listOf("تحويل", "تعديل", "حفظ", "مشاركة")
                            items(imgActions) { action ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Accent.copy(alpha = 0.12f))
                                        .border(0.8.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                        .clickable {
                                            when (action) {
                                                
                                                "تعديل" -> onChoiceClick("عدّلها")
                                                "حفظ" -> com.gptplus18.app.util.MediaShareHelper.saveToGallery(_imgCtx, imageUrl, "image") { ok ->
                                                    android.widget.Toast.makeText(_imgCtx, if (ok) "✅ تم حفظ الصورة بنجاح" else "❌ فشل الحفظ", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                                "مشاركة" -> _imgScope.launch {
                                                    com.gptplus18.app.util.MediaShareHelper.shareMedia(_imgCtx, imageUrl, "image")
                                                }
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(action, color = Accent, fontSize = 9.1.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                    }
                    if (audioUrl != null) {
                        AudioPlayerCard(
                            audioUrl = audioUrl,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                    }

                    if (cleanText.isNotBlank()) {
                        // ✅ Markdown دائم — سواء أثناء البث أو بعده
                        SelectionContainer {
                            MarkdownText(cleanText, textColor = TextPrimary, fontSize = 11)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                MessageTimestamp(msg.ts)

                ActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = stringResource(R.string.t_003),
                    onClick = { _copiedFlash = true; onCopy(cleanText) },
                )

                if (isUser) {
                    ActionButton(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.t_063),
                        onClick = { onEdit(msg) },
                    )
                } else {
                    // 👍 / 👎 / 🔗 لردود AI
                    var reaction by remember { mutableStateOf(0) }
                    val shareCtx = LocalContext.current

                    ReactionButton(
                        icon = androidx.compose.material.icons.Icons.Outlined.ThumbUp,
                        active = reaction == 1,
                        onClick = { reaction = if (reaction == 1) 0 else 1 },
                    )
                    ReactionButton(
                        icon = androidx.compose.material.icons.Icons.Outlined.ThumbDown,
                        active = reaction == -1,
                        onClick = { reaction = if (reaction == -1) 0 else -1 },
                    )
                    ReactionButton(
                        icon = androidx.compose.material.icons.Icons.Outlined.Link,
                        active = false,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, cleanText)
                                }
                                shareCtx.startActivity(Intent.createChooser(intent, null))
                            } catch (_: Exception) {}
                        },
                    )
                }
            }
        }
    }
}

