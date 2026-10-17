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

// ═══════════════════════════════════════════
// ألوان طبيعية (VSCode Dark+ / GitHub Dark)
// ═══════════════════════════════════════════
object CodeColors {
    // 🌙 Dark (VSCode Dark+)
    val Keyword = Color(0xFFC678DD)
    val String = Color(0xFF98C379)
    val Comment = Color(0xFF7F848E)
    val Number = Color(0xFFD19A66)
    val Function = Color(0xFF61AFEF)
    val Type = Color(0xFFE5C07B)
    val Default = Color(0xFFD4D4D4)
    val Operator = Color(0xFF56B6C2)
    val KeywordBold = Color(0xFFC678DD)

    // ☀️ Light (VSCode Light+)
    val LightKeyword = Color(0xFFAF00DB)
    val LightString = Color(0xFF22863A)
    val LightComment = Color(0xFF6A737D)
    val LightNumber = Color(0xFFB31D28)
    val LightFunction = Color(0xFF005CC5)
    val LightType = Color(0xFF6F42C1)
    val LightDefault = Color(0xFF24292E)
    val LightOperator = Color(0xFF005CC5)
    val LightKeywordBold = Color(0xFFAF00DB)
}

object CodeBlockColors {
    // 🌙 Dark
    val Background = Color(0xFF1A1A1A)      // ⭐ v18.9: رمادي موحّد
    val HeaderBg = Color(0xFF0D0D0D)        // ⭐ v18.9: أغمق قليلاً
    val Border = Color(0xFF444444)          // ⭐ v18.9: حدود واضحة
    val DotRed = Color(0xFFFF5F57)
    val DotYellow = Color(0xFFFFBD2E)
    val DotGreen = Color(0xFF28C840)
    val LangAccent = Color(0xFF7DD3FC)
    val CopyBg = Color(0xFF0D0D0D)          // ⭐ v18.9: زر النسخ
    val Shadow = Color(0x33000000)

    // ☀️ Light
    val LightBackground = Color(0xFFF6F8FA)
    val LightHeaderBg = Color(0xFFEAEEF3)
    val LightBorder = Color(0xFFD0D7DE)
    val LightDotRed = Color(0xFFFF5F57)
    val LightDotYellow = Color(0xFFFFBD2E)
    val LightDotGreen = Color(0xFF28C840)
    val LightLangAccent = Color(0xFF0969DA)
    val LightCopyBg = Color(0xFFDDE4EB)
    val LightShadow = Color(0x14000000)
}

@Composable
fun MarkdownText(
    text: String,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: Int = 11,
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
                        1 -> fontSize + 14
                        2 -> fontSize + 10
                        3 -> fontSize + 7
                        else -> fontSize + 4
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
                    fontWeight = FontWeight.Black,    // ⭐ v5.0.5: كان Bold
                    lineHeight = (fontSize + 9).sp,    // ⭐ v5.0.5: كان +8
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
    val cellSize = (fontSize - 1).sp
    val headerSize = (fontSize + 1).sp
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

private sealed class MdBlock {
    data class Paragraph(val content: String) : MdBlock()
    data class CodeBlock(val lang: String, val content: String) : MdBlock()
    data class Heading(val level: Int, val content: String) : MdBlock()
    data class Numbered(val num: String, val content: String) : MdBlock()
    data class Bullet(val content: String, val level: Int = 1) : MdBlock()
    data class Quote(val content: String, val type: String = "default") : MdBlock()
    data class Image(val alt: String, val url: String) : MdBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MdBlock()
    data class Checklist(val checked: Boolean, val content: String) : MdBlock()
    data class HRule(val dummy: Boolean = true) : MdBlock()
    data class Latex(val code: String) : MdBlock()
    data class LuxuryDivider(val style: String) : MdBlock()   // "bold" | "thin" | "diamond"
    data class SectionHeading(val num: String, val title: String) : MdBlock()
    data class AlertBox(val icon: String, val label: String, val content: String) : MdBlock()
}

private fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    val lines = raw.split("\n")
    val blocks = mutableListOf<MdBlock>()
    var inCode = false
    var codeLang = ""
    val codeBuf = StringBuilder()
    val paraBuf = StringBuilder()
    val tableBuf = mutableListOf<String>()

    fun flushTable() {
        if (tableBuf.isEmpty()) return
        val rows = tableBuf.map { ln ->
            ln.trim().trim('|').split("|").map { it.trim() }
        }
        val filtered = rows.filterNot { r ->
            r.isNotEmpty() && r.all { cell -> cell.matches(Regex("^:?-+:?$")) }
        }
        if (filtered.isNotEmpty()) {
            blocks.add(MdBlock.Table(filtered[0], filtered.drop(1)))
        }
        tableBuf.clear()
    }

    fun flushPara() {
        if (paraBuf.isNotBlank()) {
            // ⭐ X2: اكتشف الصور في النص أولاً
            val rawPara = paraBuf.toString().trimEnd()

            // 🔬 كشف LaTeX أولاً ($$...$$) — نستخدم raw string لتجنب escapes
            val latexRegex = Regex("""\$\$([^\$]+)\$\$""", RegexOption.DOT_MATCHES_ALL)
            val latexMatches = latexRegex.findAll(rawPara).toList()
            if (latexMatches.isNotEmpty()) {
                var lastIdx = 0
                for (m in latexMatches) {
                    val before = rawPara.substring(lastIdx, m.range.first).trim()
                    if (before.isNotBlank()) {
                        blocks.add(MdBlock.Paragraph(before))
                    }
                    val latex = m.groupValues[1].trim()
                    if (latex.isNotBlank()) {
                        blocks.add(MdBlock.Latex(latex))
                    }
                    lastIdx = m.range.last + 1
                }
                val after = rawPara.substring(lastIdx).trim()
                if (after.isNotBlank()) {
                    blocks.add(MdBlock.Paragraph(after))
                }
                paraBuf.clear()
                return
            }
            val imgRegex = Regex("""!\[([^\]]*)\]\(([^)]+)\)""")
            val imgMatches = imgRegex.findAll(rawPara).toList()
            if (imgMatches.isNotEmpty()) {
                var lastIdx = 0
                for (m in imgMatches) {
                    val before = rawPara.substring(lastIdx, m.range.first).trim()
                    if (before.isNotBlank()) {
                        blocks.add(MdBlock.Paragraph(before))
                    }
                    val alt = m.groupValues.getOrNull(1) ?: ""
                    val url = m.groupValues.getOrNull(2) ?: ""
                    if (url.isNotBlank()) {
                        blocks.add(MdBlock.Image(alt, url))
                    }
                    lastIdx = m.range.last + 1
                }
                val after = rawPara.substring(lastIdx).trim()
                if (after.isNotBlank()) {
                    blocks.add(MdBlock.Paragraph(after))
                }
            } else {
                blocks.add(MdBlock.Paragraph(rawPara))
            }
            paraBuf.clear()
        }
    }

    val headingRegex = Regex("^(#{1,4})\\s*(.+)$")
    val numberedRegex = Regex("^(\\d{1,2})[.)]\\s*(.+)$")

    lines.forEach { line ->
        var trimmed = line.trimStart()
        // Table row detection (skip inside code blocks)
        if (!inCode && trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.length >= 3) {
            flushPara()
            tableBuf.add(trimmed)
            return@forEach
        } else if (tableBuf.isNotEmpty()) {
            flushTable()
        }
        // Normalize: insert space after # / - / bullet / digits if missing
        if (trimmed.length >= 2) {
            val c0 = trimmed[0]
            val c1 = trimmed[1]
            if (c0 == '-' && c1 != ' ') {
                trimmed = "- " + trimmed.substring(1)
            } else if (c0 == '•' && c1 != ' ') {
                trimmed = "• " + trimmed.substring(1)
            } else if (c0 == '#') {
                var idx = 0
                while (idx < trimmed.length && trimmed[idx] == '#') idx++
                if (idx in 1..4 && idx < trimmed.length && trimmed[idx] != ' ') {
                    trimmed = trimmed.substring(0, idx) + " " + trimmed.substring(idx)
                }
            } else if (c0.isDigit()) {
                val m = Regex("^(\\d{1,2})([.)])(\\S)").find(trimmed)
                if (m != null) {
                    val numLen = m.groupValues[1].length
                    trimmed = trimmed.substring(0, numLen + 1) + " " + trimmed.substring(numLen + 1)
                }
            }
        }
        val headingM = headingRegex.find(trimmed)
        val numberedM = numberedRegex.find(trimmed)
        when {
            trimmed.startsWith("```") -> {
                if (inCode) {
                    blocks.add(MdBlock.CodeBlock(codeLang, codeBuf.toString().trimEnd()))
                    codeBuf.clear()
                    inCode = false
                    codeLang = ""
                } else {
                    flushPara()
                    codeLang = trimmed.substring(3).trim()
                    inCode = true
                }
            }
            inCode -> codeBuf.appendLine(line)
            headingM != null -> {
                flushPara()
                blocks.add(MdBlock.Heading(headingM.groupValues[1].length, headingM.groupValues[2].trim()))
            }
            numberedM != null -> {
                flushPara()
                blocks.add(MdBlock.Numbered(numberedM.groupValues[1], numberedM.groupValues[2].trim()))
            }
            // ═══ Luxury dividers ═══
            Regex("^━{3,}$").matches(trimmed) -> {
                flushPara()
                blocks.add(MdBlock.LuxuryDivider("bold"))
            }
            Regex("^─{3,}$").matches(trimmed) -> {
                flushPara()
                blocks.add(MdBlock.LuxuryDivider("thin"))
            }
            Regex("^[◆◇]{3,}$").matches(trimmed) -> {
                flushPara()
                blocks.add(MdBlock.LuxuryDivider("diamond"))
            }
            // ═══ Section headings ❶ ❷ ❸ ... ═══
            Regex("^([❶❷❸❹❺❻❼❽❾❿])\\s*(.+)$").find(trimmed) != null -> {
                flushPara()
                val m = Regex("^([❶❷❸❹❺❻❼❽❾❿])\\s*(.+)$").find(trimmed)!!
                blocks.add(MdBlock.SectionHeading(m.groupValues[1], m.groupValues[2].trim()))
            }
            // ═══ Alert boxes 💡 ⚠️ 🔸 📌 🎯 ═══
            Regex("^(💡|⚠️|🔸|📌|🎯|⭐|🔥|❤️|✨|🌟)\\s*([^:]*):\\s*(.+)$").find(trimmed) != null -> {
                flushPara()
                val m = Regex("^(💡|⚠️|🔸|📌|🎯|⭐|🔥|❤️|✨|🌟)\\s*([^:]*):\\s*(.+)$").find(trimmed)!!
                blocks.add(MdBlock.AlertBox(
                    m.groupValues[1],
                    m.groupValues[2].trim(),
                    m.groupValues[3].trim()
                ))
            }
            // ═══ Standard HRule ═══
            trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                flushPara()
                blocks.add(MdBlock.HRule())
            }
            trimmed.startsWith("- [ ]") || trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") -> {
                flushPara()
                val checked = trimmed[3] == 'x' || trimmed[3] == 'X'
                val rest = trimmed.substring(5).trim()
                blocks.add(MdBlock.Checklist(checked, rest))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                flushPara()
                val rawIndent = line.length - line.trimStart().length
                val lvl = when {
                    rawIndent >= 4 -> 3
                    rawIndent >= 2 -> 2
                    else -> 1
                }
                blocks.add(MdBlock.Bullet(trimmed.substring(2).trim(), lvl))
            }
            trimmed.startsWith(">") -> {
                flushPara()
                val body = trimmed.removePrefix(">").trim()
                val typeRegex = Regex("^\\[!(NOTE|WARNING|DANGER|TIP|IMPORTANT)\\]\\s*", RegexOption.IGNORE_CASE)
                val m = typeRegex.find(body)
                if (m != null) {
                    val t = m.groupValues[1].uppercase()
                    val rest = body.substring(m.range.last + 1).trim()
                    blocks.add(MdBlock.Quote(rest, t))
                } else {
                    blocks.add(MdBlock.Quote(body, "default"))
                }
            }
            line.isBlank() -> flushPara()
            else -> {
                if (paraBuf.isNotEmpty()) paraBuf.append("\n")
                paraBuf.append(line)
            }
        }
    }
    flushPara()
    flushTable()
    if (inCode && codeBuf.isNotBlank()) {
        blocks.add(MdBlock.CodeBlock(codeLang, codeBuf.toString().trimEnd()))
    }
    return blocks
}

private val SUPERSCRIPT_MAP = mapOf(
    '0' to '\u2070', '1' to '\u00b9', '2' to '\u00b2', '3' to '\u00b3',
    '4' to '\u2074', '5' to '\u2075', '6' to '\u2076', '7' to '\u2077',
    '8' to '\u2078', '9' to '\u2079', '+' to '\u207a', '-' to '\u207b',
    '(' to '\u207d', ')' to '\u207e', 'n' to '\u207f',
)
private val SUBSCRIPT_MAP = mapOf(
    '0' to '\u2080', '1' to '\u2081', '2' to '\u2082', '3' to '\u2083',
    '4' to '\u2084', '5' to '\u2085', '6' to '\u2086', '7' to '\u2087',
    '8' to '\u2088', '9' to '\u2089', '+' to '\u208a', '-' to '\u208b',
    '(' to '\u208d', ')' to '\u208e',
)

/**
 * يحوّل x^2 إلى x² و x_1 إلى x₁ (Unicode)
 * يعمل على الحروف والأرقام المفردة فقط لتجنب الأخطاء
 */
private fun beautifyMath(text: String): String {
    if (text.isEmpty()) return text
    val sb = StringBuilder(text.length)
    var i = 0
    while (i < text.length) {
        val c = text[i]
        if ((c == '^' || c == '_') && i + 1 < text.length) {
            val map = if (c == '^') SUPERSCRIPT_MAP else SUBSCRIPT_MAP
            var j = i + 1
            val collected = StringBuilder()
            while (j < text.length && map[text[j]] != null) {
                collected.append(map[text[j]]!!)
                j++
            }
            if (collected.isNotEmpty()) {
                sb.append(collected)
                i = j
                continue
            }
        }
        sb.append(c)
        i++
    }
    return sb.toString()
}

private fun inlineMarkdown(textRaw: String, baseColor: Color, isLight: Boolean = false): AnnotatedString {
    if (textRaw.contains("[[حساس_")) {
        return SensitiveMarkers.apply(textRaw, baseColor)
    }
    val text = beautifyMath(textRaw)
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end > 0) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFF5EBF7A))) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            if (i + 1 < text.length && text[i] == '_' && text[i + 1] == '_') {
                val end = text.indexOf("__", i + 2)
                if (end > 0) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFF5EBF7A))) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            if (text[i] == '_') {
                val end = text.indexOf('_', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }
            if (i + 1 < text.length && text[i] == '=' && text[i + 1] == '=') {
                val end = text.indexOf("==", i + 2)
                if (end > 0) {
                    withStyle(
                        SpanStyle(
                            background = Color(0xFFFFEB3B).copy(alpha = 0.25f),
                            color = baseColor,
                        ),
                    ) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            if (text[i] == '~' && i + 1 < text.length && text[i + 1] == '~') {
                val end = text.indexOf("~~", i + 2)
                if (end > 0) {
                    withStyle(
                        SpanStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            color = baseColor,
                        ),
                    ) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            if (text[i] == '[') {
                val closeBracket = text.indexOf(']', i + 1)
                if (closeBracket > i + 1 && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen > closeBracket + 2) {
                        val linkText = text.substring(i + 1, closeBracket)
                        val url = text.substring(closeBracket + 2, closeParen)
                        try {
                            withLink(
                                LinkAnnotation.Url(
                                    url = url,
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            color = Color(0xFF4A9EFF),
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                        )
                                    )
                                )
                            ) {
                                append(linkText)
                            }
                            i = closeParen + 1
                            continue
                        } catch (_: Throwable) {
                            // fallthrough to plain render if link not supported
                        }
                    }
                }
            }
            if (text[i] == '*') {
                val end = text.indexOf('*', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }
            // ⭐ v5.0.6: auto-linkify روابط https:// و http://
            if (i + 7 < text.length &&
                (text.startsWith("https://", i) || text.startsWith("http://", i))
            ) {
                val endMatch = Regex("^https?://[^\\s\\)\\]\\}\u060C\u061B\u061F،؛؟]+")
                    .find(text.substring(i))
                if (endMatch != null) {
                    val url = endMatch.value
                    try {
                        withLink(
                            LinkAnnotation.Url(
                                url = url,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = Color(0xFF4A9EFF),
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                    )
                                )
                            )
                        ) {
                            append(url)
                        }
                    } catch (_: Throwable) {
                        withStyle(SpanStyle(color = Color(0xFF4A9EFF))) { append(url) }
                    }
                    i += url.length
                    continue
                }
            }
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end > i + 1) {
                    // ⭐ v5.0.6: inline code → أحمر
                    val _inlineCodeColor = if (isLight) Color(0xFFDC2626) else Color(0xFFFF6B6B)
                    val _inlineCodeBg = if (isLight) Color(0xFFFEE2E2) else Color(0xFF2A1616)
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = _inlineCodeColor,
                            fontSize = (11).sp,
                        ),
                    ) {
                        append(" ")
                        append(text.substring(i + 1, end))
                        append(" ")
                    }
                    i = end + 1
                    continue
                }
            }
            withStyle(SpanStyle(color = baseColor)) { append(text[i]) }
            i++
        }
    }
}
