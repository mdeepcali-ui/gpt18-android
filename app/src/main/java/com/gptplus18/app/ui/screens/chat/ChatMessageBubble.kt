package com.gptplus18.app.ui.screens.chat

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

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatMessageBubble — Message Rendering Components
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.8
 * ═══════════════════════════════════════════════════════════════
 *
 *  مكوّنات عرض الرسائل:
 *    • CodeFilesCard    — بطاقة ملفات الكود
 *    • UserMessageText  — رسالة المستخدم (مع طي)
 *    • MessageBubble    — فقاعة الرسالة الرئيسية (public)
 *
 *  ⚠️ MessageBubble public — تُستخدم من ChatScreen + CodeScreen
 */


// ⭐ v2.0: بطاقة ملفات الكود الأنيقة
@Composable
internal fun CodeFilesCard(
    files: List<com.gptplus18.app.data.api.CodeFileItem>,
    zipUrl: String?,
) {
    val ctx = LocalContext.current
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1F1F26), Color(0xFF141419))
                )
            )
            .border(
                width = 0.6.dp,
                color = Color(0xFF3A3C40),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(14.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📁", fontSize = 12.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الملفات جاهزة",
                        color = TextPrimary,
                        fontSize = 9.8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${files.size} ملف${if (zipUrl != null) " — أو حمّل الكل" else ""}",
                        color = TextSecondary,
                        fontSize = 9.sp,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // قائمة الملفات
            files.forEach { f ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.4f))
                        .clickable {
                            try {
                                val i = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(f.url))
                                ctx.startActivity(i)
                            } catch (_: Exception) {}
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("📄", fontSize = 10.6.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = f.name,
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Text(
                        text = "⬇",
                        color = Accent,
                        fontSize = 9.8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // زر ZIP
            if (zipUrl != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Accent, Accent.copy(alpha = 0.8f))
                            )
                        )
                        .clickable {
                            try {
                                val i = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(zipUrl))
                                ctx.startActivity(i)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 11.dp),
                ) {
                    Text("📦", fontSize = 10.6.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "تحميل الكل (ZIP)",
                        color = Color.Black,
                        fontSize = 9.1.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}


