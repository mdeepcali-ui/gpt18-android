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
fun MarkdownText(
    text: String,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: Int = 14,
) {
    val blocks = parseMarkdownBlocks(text)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Image -> {
                    coil.compose.AsyncImage(
                        model = block.url,
                        contentDescription = block.alt.ifBlank { "صورة" },
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 320.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .clickable { onImageClick(block.url) },
                    )
                    Spacer(Modifier.height(6.dp))
                }
                is MdBlock.CodeBlock -> {
                    Spacer(Modifier.height(4.dp))
                    if (block.lang.equals("long", ignoreCase = true)) {
                        TextBlockView(block.content)
                    } else {
                        CodeBlockView(block.lang, block.content)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                is MdBlock.Heading -> {
                    val size = when (block.level) {
                        1 -> fontSize + 18   // H1: 34sp
                        2 -> fontSize + 14   // H2: 30sp
                        3 -> fontSize + 10   // H3: 26sp
                        else -> fontSize + 6 // H4: 22sp
                    }
                    val topPad = when (block.level) {
                        1 -> 18.dp
                        2 -> 16.dp
                        3 -> 14.dp
                        else -> 12.dp
                    }
                    val accentColor = when (block.level) {
                        1 -> Accent
                        2 -> Accent.copy(alpha = 0.75f)
                        3 -> Accent.copy(alpha = 0.55f)
                        else -> Accent.copy(alpha = 0.4f)
                    }
                    val barWidth = when (block.level) {
                        1 -> 4.dp
                        2 -> 3.dp
                        else -> 2.dp
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = topPad, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height((size + 4).dp)
                                .background(accentColor, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = inlineMarkdown(block.content, textColor, LocalAppColors.current.isLight),
                            color = textColor,
                            fontWeight = FontWeight.Black,
                            fontSize = size.sp,
                            lineHeight = (size + 13).sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.Bullet -> {
                    val bulletChar = when (block.level) {
                        1 -> "●"
                        2 -> "○"
                        else -> "▪"
                    }
                    val bulletColor = when (block.level) {
                        1 -> Accent
                        2 -> Accent.copy(alpha = 0.75f)
                        else -> TextSecondary
                    }
                    val startPad = when (block.level) {
                        1 -> 6.dp
                        2 -> 22.dp
                        else -> 38.dp
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = startPad, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = bulletChar,
                            color = bulletColor,
                            fontSize = (fontSize - 4).sp,
                            modifier = Modifier.padding(top = 5.dp, end = 10.dp),
                        )
                        Text(
                            text = inlineMarkdown(block.content, textColor, LocalAppColors.current.isLight),
                            color = textColor,
                            fontSize = fontSize.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = (fontSize + 11).sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.Numbered -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, top = 6.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = block.num + ".",
                        color = Accent,
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 10.dp),
                    )
                    Text(
                        text = inlineMarkdown(block.content, textColor, LocalAppColors.current.isLight),
                        color = textColor,
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = (fontSize + 11).sp,
                        modifier = Modifier.weight(1f),
                    )
                }
                is MdBlock.Quote -> {
                    val (icon, label, tintColor) = when (block.type) {
                        "NOTE"      -> Triple("\u2139\ufe0f", "\u0645\u0644\u0627\u062d\u0638\u0629", Color(0xFF4A9EFF))
                        "WARNING"   -> Triple("\u26a0\ufe0f", "\u062a\u062d\u0630\u064a\u0631", Color(0xFFFFB800))
                        "DANGER"    -> Triple("\uD83D\uDEA8", "\u062e\u0637\u0631", Color(0xFFEF4444))
                        "TIP"       -> Triple("\uD83D\uDCA1", "\u0646\u0635\u064a\u062d\u0629", Color(0xFF22C55E))
                        "IMPORTANT" -> Triple("\uD83D\uDD25", "\u0645\u0647\u0645", Color(0xFFA855F7))
                        else        -> Triple("", "", TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F0F12))
                            .border(
                                width = 1.dp,
                                color = if (tintColor == TextSecondary) TextSecondary.copy(alpha = 0.35f) else tintColor.copy(alpha = 0.5f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                            ),
                    ) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(tintColor)
                                    .heightIn(min = 40.dp),
                            )
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                if (label.isNotBlank()) {
                                    Text(
                                        text = "$icon $label",
                                        color = tintColor,
                                        fontSize = (fontSize - 2).sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    )
                                }
                                Text(
                                    text = inlineMarkdown(block.content, TextSecondary, LocalAppColors.current.isLight),
                                    color = TextSecondary,
                                    fontSize = (fontSize - 1).sp,
                                    lineHeight = (fontSize + 9).sp,
                                )
                            }
                        }
                    }
                }
                is MdBlock.Latex -> {
                    LatexView(
                        latex = block.code,
                        textColor = textColor,
                        fontSize = fontSize + 4,
                    )
                }
                is MdBlock.HRule -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .height(1.dp)
                            .background(TextSecondary.copy(alpha = 0.25f)),
                    )
                }
                is MdBlock.LuxuryDivider -> {
                    when (block.style) {
                        "bold" -> {
                            // ━━━━━━━━ — خط ذهبي عريض
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp, bottom = 10.dp)
                                    .height(2.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Accent.copy(alpha = 0.8f),
                                                Accent.copy(alpha = 0.8f),
                                                Color.Transparent,
                                            )
                                        )
                                    ),
                            )
                        }
                        "thin" -> {
                            // ────── — خط رفيع بشفافية
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 6.dp)
                                    .height(1.dp)
                                    .background(TextSecondary.copy(alpha = 0.35f)),
                            )
                        }
                        "diamond" -> {
                            // ◆◆◆ — زخرفة ذهبية
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "◆  ◆  ◆",
                                    color = Accent,
                                    fontSize = 9.sp,
                                    letterSpacing = 6.sp,
                                )
                            }
                        }
                    }
                }
                is MdBlock.SectionHeading -> {
                    // ❶ العنوان
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // حرف مرقّم بحجم كبير ولون
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Accent.copy(alpha = 0.15f))
                                .border(1.dp, Accent.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = block.num,
                                color = Accent,
                                fontSize = 10.6.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = inlineMarkdown(block.title, textColor, LocalAppColors.current.isLight),
                            color = textColor,
                            fontWeight = FontWeight.Black,
                            fontSize = (fontSize + 2).sp,
                            lineHeight = (fontSize + 11).sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.AlertBox -> {
                    val (tintColor, bgColor) = when (block.icon) {
                        "💡" -> Color(0xFFFBBF24) to Color(0xFF1F1A08)   // ذهبي
                        "⚠️" -> Color(0xFFF59E0B) to Color(0xFF1F1508)   // برتقالي
                        "🔸" -> Color(0xFF60A5FA) to Color(0xFF0A1424)   // أزرق
                        "📌" -> Color(0xFFA78BFA) to Color(0xFF150A24)   // بنفسجي
                        "🎯" -> Color(0xFF34D399) to Color(0xFF0A1F15)   // أخضر
                        "⭐" -> Color(0xFFFBBF24) to Color(0xFF1F1A08)
                        "🔥" -> Color(0xFFEF4444) to Color(0xFF1F0A0A)
                        "❤️" -> Color(0xFFEC4899) to Color(0xFF1F0A14)
                        "✨" -> Color(0xFFA78BFA) to Color(0xFF150A24)
                        "🌟" -> Color(0xFFFBBF24) to Color(0xFF1F1A08)
                        else -> TextSecondary to Color(0xFF0F0F12)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp, end = 2.dp, top = 6.dp, bottom = 6.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .border(
                                width = 1.dp,
                                color = tintColor.copy(alpha = 0.4f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                            ),
                    ) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(tintColor)
                                    .heightIn(min = 40.dp),
                            )
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = block.icon,
                                    fontSize = (fontSize + 1).sp,
                                    modifier = Modifier.padding(end = 8.dp, top = 1.dp),
                                )
                                Column {
                                    if (block.label.isNotBlank()) {
                                        Text(
                                            text = block.label,
                                            color = tintColor,
                                            fontSize = (fontSize - 2).sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(bottom = 3.dp),
                                        )
                                    }
                                    Text(
                                        text = inlineMarkdown(block.content, textColor, LocalAppColors.current.isLight),
                                        color = textColor,
                                        fontSize = (fontSize - 1).sp,
                                        lineHeight = (fontSize + 9).sp,
                                    )
                                }
                            }
                        }
                    }
                }
                is MdBlock.Checklist -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp, end = 10.dp)
                            .size((fontSize - 2).dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.5.dp, if (block.checked) Accent else TextSecondary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .background(if (block.checked) Accent.copy(alpha = 0.15f) else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (block.checked) {
                            Text(
                                text = "✓",
                                color = Accent,
                                fontSize = (fontSize - 5).sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Text(
                        text = inlineMarkdown(block.content, if (block.checked) TextSecondary else textColor, LocalAppColors.current.isLight),
                        color = if (block.checked) TextSecondary else textColor,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize + 11).sp,
                        textDecoration = if (block.checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f),
                    )
                }
                is MdBlock.Table -> {
                    Spacer(Modifier.height(4.dp))
                    TableView(block.headers, block.rows, fontSize)
                    Spacer(Modifier.height(4.dp))
                }
                is MdBlock.Paragraph -> Text(
                    text = inlineMarkdown(block.content, textColor, LocalAppColors.current.isLight),
                    color = textColor,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Normal,   // ⭐ v5.3.22: نص عادي أنعم
                    lineHeight = (fontSize + 8).sp,   // ⭐ v5.3.22: مسافة مريحة
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),     // ⭐ v5.0.5: أوضح
                )
            }
        }
    }
}

@Composable
private fun TextBlockView(text: String) {
    val clip = LocalClipboardManager.current
    val ctx = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D0D0D))
                .border(1.dp, Color(0xFF2A2D38), RoundedCornerShape(12.dp)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0D0D))
                        .padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Outlined.Article,
                        contentDescription = null,
                        tint = Color(0xFF7DD3FC),
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "\u0646\u0635",
                        color = Color(0xFF7DD3FC),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0D0D0D))
                            .clickable {
                                clip.setText(AnnotatedString(text))
                                android.widget.Toast.makeText(
                                    ctx,
                                    "\u2705 \u062a\u0645 \u0627\u0644\u0646\u0633\u062e",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ContentCopy,
                            stringResource(R.string.t_003),
                            tint = Accent,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            stringResource(R.string.t_003),
                            color = Accent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    MarkdownText(
                        text = text,
                        textColor = Color(0xFFE5E7EB),
                        fontSize = 10,
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(lang: String, code: String) {
    val clip = LocalClipboardManager.current
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()
    val isLight = LocalAppColors.current.isLight

    // ⭐ طي الكود الطويل
    val MAX_LINES = 20
    val allLines = code.lines()
    val totalLines = allLines.size
    val shouldCollapse = totalLines > MAX_LINES
    var expanded by remember { mutableStateOf(false) }
    val displayCode = if (shouldCollapse && !expanded) {
        allLines.take(MAX_LINES).joinToString("\n")
    } else code

    // ☀️🌙 ألوان ديناميكية
    val bgColor = if (isLight) CodeBlockColors.LightBackground else CodeBlockColors.Background
    val headerBgColor = if (isLight) CodeBlockColors.LightHeaderBg else CodeBlockColors.HeaderBg
    val borderColor = if (isLight) CodeBlockColors.LightBorder else CodeBlockColors.Border
    val copyBgColor = if (isLight) CodeBlockColors.LightCopyBg else CodeBlockColors.CopyBg
    val langAccentColor = if (isLight) CodeBlockColors.LightLangAccent else CodeBlockColors.LangAccent

    val langDisplay = when (lang.lowercase()) {
        "python", "py" -> "Python"
        "javascript", "js" -> "JavaScript"
        "typescript", "ts" -> "TypeScript"
        "kotlin", "kt" -> "Kotlin"
        "java" -> "Java"
        "html" -> "HTML"
        "css" -> "CSS"
        "sql" -> "SQL"
        "bash", "sh" -> "Bash"
        "json" -> "JSON"
        "xml" -> "XML"
        "yaml", "yml" -> "YAML"
        "go" -> "Go"
        "rust", "rs" -> "Rust"
        "cpp" -> "C++"
        "c" -> "C"
        "php" -> "PHP"
        "ruby", "rb" -> "Ruby"
        "swift" -> "Swift"
        "" -> "Code"
        else -> lang.replaceFirstChar { it.uppercase() }
    }
    val langIcon = when (lang.lowercase()) {
        "python", "py" -> "🐍"
        "javascript", "js" -> "JS"
        "typescript", "ts" -> "TS"
        "kotlin", "kt" -> "K"
        "java" -> "☕"
        "html" -> "🌐"
        "css" -> "🎨"
        "sql" -> "🗃"
        "bash", "sh" -> "$"
        "json" -> "{}"
        "xml" -> "<>"
        "yaml", "yml" -> "≡"
        "go" -> "Go"
        "rust", "rs" -> "R"
        "cpp" -> "++"
        "c" -> "C"
        "php" -> "🐘"
        "ruby", "rb" -> "◆"
        "swift" -> "🦅"
        else -> "⟨/⟩"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        ) {
            Column {
                // ── Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBgColor)
                        .padding(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // ⭐ v9.1: زر النسخ على اليسار (عكس السابق)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(copyBgColor)
                            .clickable {
                                clip.setText(AnnotatedString(code))
                                android.widget.Toast.makeText(
                                    ctx,
                                    "✅ تم النسخ",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            stringResource(R.string.t_003),
                            tint = Accent,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            stringResource(R.string.t_003),
                            color = Accent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    // ⭐ v9.1: شارة اللغة على اليمين + نقطة أنيقة
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(langAccentColor.copy(alpha = 0.1f))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(langAccentColor)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = langDisplay,
                            color = langAccentColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp,
                        )
                    }
                }
                // ── Body ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = highlightSyntax(displayCode, lang, isLight),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.1.sp,
                        lineHeight = 13.sp,
                        softWrap = false,
                        style = TextStyle(textDirection = TextDirection.Ltr),
                    )
                }
                // ── Footer طي ──
                if (shouldCollapse) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerBgColor)
                            .clickable { expanded = !expanded }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (expanded)
                                "▲ طيّ الكود"
                            else
                                "▼ عرض الكود كامل ($totalLines سطر)",
                            color = langAccentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableView(
    headersIn: List<String>,
    rowsIn: List<List<String>>,
    fontSize: Int,
) {
    val cellSize = 13.sp
    val headerSize = 14.sp
    val isLight = LocalAppColors.current.isLight

    // ═══════════════════════════════════════════════════════
    // ⭐ v5.0.8: نحذف الأعمدة الفاضية قبل العرض
    // ═══════════════════════════════════════════════════════
    val totalCols = maxOf(headersIn.size, rowsIn.maxOfOrNull { it.size } ?: 0)

    // أي عمود فيه محتوى في أي صف (أو في الهيدر)؟
    val keepCol = BooleanArray(totalCols) { false }
    for (i in 0 until totalCols) {
        val h = headersIn.getOrNull(i)?.trim().orEmpty()
        if (h.isNotEmpty()) { keepCol[i] = true; continue }
        for (row in rowsIn) {
            val c = row.getOrNull(i)?.trim().orEmpty()
            if (c.isNotEmpty()) { keepCol[i] = true; break }
        }
    }

    // نبني القوائم الجديدة بدون الأعمدة الفاضية
    val keepIdx = (0 until totalCols).filter { keepCol[it] }

    // إذا كلها فاضية → لا نعرض الجدول
    if (keepIdx.isEmpty()) return

    val headers = keepIdx.map { headersIn.getOrNull(it).orEmpty() }
    val rows = rowsIn.map { row ->
        keepIdx.map { row.getOrNull(it).orEmpty() }
    }

    // ═══════════════════════════════════════════════════════
    // الألوان
    // ═══════════════════════════════════════════════════════
    val bgColor       = if (isLight) Color(0xFFFAFAFC) else Color(0xFF0D0D0D)  // ⭐ v18.9
    val headerBg      = if (isLight) Color(0x1A6B4FDB) else Color(0xFF0D0D0D)  // ⭐ v18.9
    val rowAltBg      = if (isLight) Color(0xFFF4F4F7) else Color(0xFF141414)  // ⭐ v18.9
    val borderCol     = if (isLight) Color(0xFFC8CCD4) else Color(0xFF444444)  // ⭐ v18.9
    val cellBorderCol = if (isLight) Color(0xFFD4D8E0) else Color(0xFF3D3D3D)  // ⭐ v18.9

    val hScroll = rememberScrollState()

    // نحسب عرض كل عمود حسب محتواه الحقيقي
    val widths = headers.indices.map { i -> _columnWidth(headers, rows, i) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .background(bgColor)
            .horizontalScroll(hScroll),
    ) {
        Column {
            // ═══ Header Row ═══
            Row(
                modifier = Modifier
                    .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                    .background(headerBg)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        text = inlineMarkdown(h, Accent, isLight),
                        color = Accent,
                        fontSize = headerSize,
                        fontWeight = FontWeight.Black,
                        lineHeight = (fontSize + 9).sp,
                        modifier = Modifier
                            .width(widths[i])
                            .padding(vertical = 6.dp)
                            .padding(end = if (i < headers.size - 1) 14.dp else 0.dp),
                    )
                    if (i < headers.size - 1) {
                        Box(Modifier.fillMaxHeight().width(1.dp).background(cellBorderCol))
                    }
                }
            }

            // ═══ فاصل الهيدر ═══
            Box(Modifier.fillMaxWidth().height(1.5.dp).background(borderCol))

            // ═══ Data Rows ═══
            rows.forEachIndexed { ri, row ->
                Row(
                    modifier = Modifier
                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                        .background(if (ri % 2 == 1) rowAltBg else Color.Transparent)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    row.forEachIndexed { i, cell ->
                        Text(
                            text = inlineMarkdown(cell, TextPrimary, isLight),
                            color = TextPrimary,
                            fontSize = cellSize,
                            lineHeight = (fontSize + 9).sp,
                            modifier = Modifier
                                .width(widths[i])
                                .padding(vertical = 6.dp)
                                .padding(end = if (i < row.size - 1) 14.dp else 0.dp),
                        )
                        if (i < row.size - 1) {
                            Box(Modifier.fillMaxHeight().width(1.dp).background(cellBorderCol))
                        }
                    }
                }
                if (ri < rows.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(cellBorderCol))
                }
            }
        }
    }
}

/**
 * ⭐ v5.0.5: يحسب عرض كل عمود حسب أطول محتوى فيه.
 * - الحد الأدنى: 80dp
 * - الحد الأقصى: 220dp
 */
private fun _columnWidth(
    headers: List<String>,
    rows: List<List<String>>,
    colIndex: Int,
): androidx.compose.ui.unit.Dp {
    // ⭐ v5.0.6: Arabic-aware width
    fun widthFor(s: String?): Int {
        if (s.isNullOrBlank()) return 0
        var w = 0
        for (c in s) {
            w += when {
                c in '\u0600'..'\u06FF' -> 13      // عربي
                c in '\u0750'..'\u077F' -> 13      // عربي موسّع
                c in '\u4E00'..'\u9FFF' -> 15      // صيني/ياباني
                c.code > 0x1F000 -> 16              // إيموجي
                c.isUpperCase() -> 9
                c == ' ' -> 5
                else -> 8
            }
        }
        return w
    }

    val headerW = widthFor(headers.getOrNull(colIndex))
    val rowW = rows.maxOfOrNull { widthFor(it.getOrNull(colIndex)) } ?: 0
    val maxW = maxOf(headerW, rowW)

    val dp = maxW.dp
    return dp.coerceIn(70.dp, 280.dp)
}


private fun highlightSyntax(code: String, lang: String, isLight: Boolean): AnnotatedString {
    // ☀️🌙 ألوان ديناميكية
    val cKeyword = if (isLight) CodeColors.LightKeyword else CodeColors.Keyword
    val cString = if (isLight) CodeColors.LightString else CodeColors.String
    val cComment = if (isLight) CodeColors.LightComment else CodeColors.Comment
    val cNumber = if (isLight) CodeColors.LightNumber else CodeColors.Number
    val cFunction = if (isLight) CodeColors.LightFunction else CodeColors.Function
    val cType = if (isLight) CodeColors.LightType else CodeColors.Type
    val cDefault = if (isLight) CodeColors.LightDefault else CodeColors.Default
    val cOperator = if (isLight) CodeColors.LightOperator else CodeColors.Operator

    return buildAnnotatedString {
        val keywords = setOf(
            "fun", "val", "var", "class", "object", "interface", "enum", "data",
            "if", "else", "when", "for", "while", "do", "return", "break", "continue",
            "try", "catch", "finally", "throw", "import", "package", "as", "is",
            "in", "out", "suspend", "override", "private", "public", "protected",
            "internal", "abstract", "open", "sealed", "companion", "init",
            "null", "true", "false", "this", "super", "by", "where", "lateinit",
            "const", "inline", "noinline", "crossinline", "reified", "typealias",
            "def", "from", "global", "nonlocal", "lambda", "yield", "with", "pass",
            "raise", "except", "async", "await", "and", "or", "not", "None", "True", "False",
            "function", "let", "new", "typeof", "instanceof", "undefined",
            "export", "default", "extends", "static", "void"
        )

        val types = setOf(
            "Int", "Long", "Double", "Float", "String", "Boolean", "Char", "Byte",
            "Short", "Unit", "Any", "Nothing", "List", "Map", "Set", "Array",
            "Number", "Object", "Function",
            "int", "long", "double", "float", "bool", "str", "dict", "list", "tuple"
        )

        var i = 0
        while (i < code.length) {
            val c = code[i]

            // Line comment
            if (i + 1 < code.length && (
                    (code[i] == '/' && code[i + 1] == '/') ||
                    (code[i] == '#' && lang.lowercase() in listOf("python", "py", "sh", "bash", "yaml", "yml"))
                )) {
                val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
                withStyle(SpanStyle(color = cComment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // Block comment
            if (i + 1 < code.length && code[i] == '/' && code[i + 1] == '*') {
                val end = code.indexOf("*/", i + 2).let { if (it == -1) code.length else it + 2 }
                withStyle(SpanStyle(color = cComment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // String
            if (c == '"' || c == '\'' || c == '`') {
                val quote = c
                var j = i + 1
                while (j < code.length) {
                    if (code[j] == '\\' && j + 1 < code.length) {
                        j += 2; continue
                    }
                    if (code[j] == quote) { j++; break }
                    if (code[j] == '\n' && quote != '`') break
                    j++
                }
                withStyle(SpanStyle(color = cString)) {
                    append(code.substring(i, j))
                }
                i = j
                continue
            }

            // Number
            if (c.isDigit()) {
                var j = i
                while (j < code.length && (code[j].isDigit() || code[j] == '.' || code[j] == '_')) j++
                withStyle(SpanStyle(color = cNumber)) {
                    append(code.substring(i, j))
                }
                i = j
                continue
            }

            // Identifier / keyword
            if (c.isLetter() || c == '_') {
                var j = i
                while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) j++
                val word = code.substring(i, j)
                val color = when {
                    keywords.contains(word) -> cKeyword
                    types.contains(word) -> cType
                    j < code.length && code[j] == '(' -> cFunction
                    word.firstOrNull()?.isUpperCase() == true && word.length > 1 -> cType
                    else -> cDefault
                }
                withStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = FontWeight.Black,
                    ),
                ) {
                    append(word)
                }
                i = j
                continue
            }

            // Operator
            when (c) {
                '+', '-', '*', '/', '%', '=', '<', '>', '!', '&', '|', '^', '~', '?' -> {
                    withStyle(SpanStyle(color = cOperator)) { append(c) }
                }
                '(', ')', '[', ']', '{', '}', ',', ';', ':', '.' -> {
                    withStyle(SpanStyle(color = cDefault)) { append(c) }
                }
                else -> {
                    withStyle(SpanStyle(color = cDefault)) { append(c) }
                }
            }
            i++
        }
    }
}

