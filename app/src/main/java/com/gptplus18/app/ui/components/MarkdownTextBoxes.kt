package com.gptplus18.app.ui.components


import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R
import androidx.compose.material.icons.outlined.Article

@Composable
fun MarkdownTextBox(
    text: String,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: Int = 11,
) {
    val important = isImportantReply(text)
    val clip = LocalClipboardManager.current

    if (!important) {
        MarkdownText(
            text = text,
            onImageClick = onImageClick,
            modifier = modifier,
            textColor = textColor,
            fontSize = fontSize,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
    ) {
        // Header مع زر نسخ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0D0D))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("\u2705", fontSize = 9.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "\u0645\u062d\u062a\u0648\u0649 \u0645\u0646\u0638\u0645",
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { clip.setText(AnnotatedString(text)) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    "\u0646\u0633\u062e",
                    tint = Accent,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("\u0646\u0633\u062e", color = Accent, fontSize = 9.sp)
            }
        }
        Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            MarkdownText(
                text = text,
                onImageClick = onImageClick,
                modifier = Modifier,
                textColor = textColor,
                fontSize = fontSize,
            )
        }
    }
}

private const val COLLAPSE_THRESHOLD = 1500

@Composable
fun CollapsibleMarkdownTextBox(
    text: String,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: Int = 11,
) {
    val needCollapse = text.length > COLLAPSE_THRESHOLD
    var expanded by remember { mutableStateOf(!needCollapse) }

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            MarkdownTextBox(
                text = text,
                onImageClick = onImageClick,
                textColor = textColor,
                fontSize = fontSize,
            )
        }
        if (needCollapse && !expanded) {
            MarkdownTextBox(
                text = text.take(600) + "\n\n...",
                onImageClick = onImageClick,
                textColor = textColor,
                fontSize = fontSize,
            )
        }
        if (needCollapse) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent.copy(alpha = 0.1f))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (expanded) "\u25b2 \u0637\u064a \u0627\u0644\u0631\u062f" else "\u25bc \u0639\u0631\u0636 \u0627\u0644\u0631\u062f \u0643\u0627\u0645\u0644\u0627\u064b (${text.length} \u062d\u0631\u0641)",
                    color = Accent,
                    fontSize = 9.1.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

private fun isImportantReply(text: String): Boolean {
    if (text.length < 100) return false
    
    // ⭐ هيكل منظم (جدول/عناوين/قوائم) — أي طول
    val hasHeading = Regex("(?m)^#{1,4}\\s").containsMatchIn(text)
    val hasTable = Regex("(?m)^\\|.+\\|").containsMatchIn(text)
    val hasQuote = Regex("(?m)^>\\s?\\[!").containsMatchIn(text)
    val numberedCount = Regex("(?m)^\\d{1,2}[.)]\\s").findAll(text).count()
    val bulletCount = Regex("(?m)^[-*]\\s").findAll(text).count()
    if (hasHeading || hasTable || hasQuote || numberedCount >= 3 || bulletCount >= 3) return true
    
    // ⭐ قصة/شعر/سرد: طويل (> 600 حرف) + فقرات متعددة (2+)
    val paragraphs = text.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() }.size
    if (text.length >= 600 && paragraphs >= 2) return true
    
    return false
}

