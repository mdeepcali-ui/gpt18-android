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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gptplus18.app.data.models.Attachment
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextTertiary

private val BgBlack = Color(0xFF000000)
private val ComposerBg = Color(0xFF0D0D0D)
private val ComposerBorder = Color(0xFF2A2A2A)
private val SendBlue = Color(0xFF0A84FF)  // أزرق زيك
private val IconGray = Color(0xFFB4B4B4)
private val CloseBtnBg = Color(0xFF4A4A4A)

@Composable
fun ChatGptComposer(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    attachments: List<Attachment>,
    onAttachClick: () -> Unit,
    onSend: () -> Unit,
    onRemoveAttachment: (Long) -> Unit,
) {
    val hasText = value.isNotBlank()
    var showEmojiSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .navigationBarsPadding(),
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
                .background(ComposerBg)
                .border(0.5.dp, ComposerBorder, RoundedCornerShape(28.dp))
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "اسأل GPT+18",
                        color = TextTertiary,
                        fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = TextPrimary,
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
                    "إيموجي",
                    tint = IconGray,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(enabled = enabled) { showEmojiSheet = true },
                )
                Spacer(Modifier.width(10.dp))

                // +
                Icon(
                    Icons.Default.Add,
                    "إرفاق",
                    tint = IconGray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = enabled, onClick = onAttachClick),
                )

                Spacer(Modifier.weight(1f))

                // إرسال (يظهر فقط عند الكتابة)
                if (hasText) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SendBlue)
                            .clickable(enabled = enabled, onClick = onSend),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            "إرسال",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D0D0D),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "الإيموجيات",
                color = Color.White,
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
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A1A)),
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
                .background(CloseBtnBg)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Close,
                "حذف",
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }

        if (attachment.isUploading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF333333)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(attachment.progress)
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
