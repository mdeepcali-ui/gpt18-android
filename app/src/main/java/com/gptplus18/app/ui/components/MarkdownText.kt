package com.gptplus18.app.ui.components

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
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
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

// ═══════════════════════════════════════════
// ألوان طبيعية (VSCode Dark+ / GitHub Dark)
// ═══════════════════════════════════════════
object CodeColors {
    val Keyword = Color(0xFF569CD6)      // أزرق عادي
    val String = Color(0xFFCE9178)       // برتقالي عادي
    val Comment = Color(0xFF6A9955)      // أخضر عادي
    val Number = Color(0xFFB5CEA8)       // أخضر فاتح عادي
    val Function = Color(0xFFDCDCAA)     // أصفر ذهبي عادي
    val Type = Color(0xFF4EC9B0)         // تركواز عادي
    val Default = Color(0xFFD4D4D4)      // أبيض عادي
    val Operator = Color(0xFFD4D4D4)     // أبيض عادي
    val KeywordBold = Color(0xFF569CD6)  // أزرق عادي
}

object CodeBlockColors {
    val Background = Color(0xFF1E1E1E)
    val HeaderBg = Color(0xFF252526)
    val Border = Color(0xFF3E3E42)
}

@Composable
fun MarkdownText(
    text: String,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: Int = 17,
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
                    CodeBlockView(block.lang, block.content)
                    Spacer(Modifier.height(4.dp))
                }
                is MdBlock.Heading -> {
                    val size = when (block.level) {
                        1 -> fontSize + 9
                        2 -> fontSize + 6
                        3 -> fontSize + 4
                        else -> fontSize + 2
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
                            text = inlineMarkdown(block.content, textColor),
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = size.sp,
                            lineHeight = (size + 8).sp,
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
                            text = inlineMarkdown(block.content, textColor),
                            color = textColor,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 9).sp,
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
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(end = 10.dp),
                    )
                    Text(
                        text = inlineMarkdown(block.content, textColor),
                        color = textColor,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize + 9).sp,
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
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    )
                                }
                                Text(
                                    text = inlineMarkdown(block.content, TextSecondary),
                                    color = TextSecondary,
                                    fontSize = (fontSize - 1).sp,
                                    lineHeight = (fontSize + 6).sp,
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
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Text(
                        text = inlineMarkdown(block.content, if (block.checked) TextSecondary else textColor),
                        color = if (block.checked) TextSecondary else textColor,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize + 9).sp,
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
                    text = inlineMarkdown(block.content, textColor),
                    color = textColor,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize + 8).sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CodeBlockView(lang: String, code: String) {
    val clip = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // ⭐ الكود دائماً LTR — حتى في واجهة عربية
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeBlockColors.Background, RoundedCornerShape(10.dp)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CodeBlockColors.HeaderBg, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "● ● ●",
                        color = Color(0xFFFF5F57),
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (lang.isBlank()) "code" else lang,
                        color = Color(0xFF858585),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            clip.setText(AnnotatedString(code))
                        },
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            stringResource(R.string.t_003),
                            tint = Accent,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.t_003), color = Accent, fontSize = 11.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(10.dp),
                ) {
                    Text(
                        text = highlightSyntax(code, lang),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        softWrap = false,
                        style = TextStyle(textDirection = TextDirection.Ltr),
                    )
                }
            }
        }
    }
}

@Composable
private fun TableView(
    headers: List<String>,
    rows: List<List<String>>,
    fontSize: Int,
) {
    val cellSize = (fontSize - 2).sp
    val headerSize = (fontSize - 2).sp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, TextSecondary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .background(Color(0xFF0F0F12)),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Accent.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            headers.forEachIndexed { i, h ->
                Text(
                    text = inlineMarkdown(h, Accent),
                    color = Accent,
                    fontSize = headerSize,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (i < headers.size - 1) 8.dp else 0.dp),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.2f)))
        // Data rows
        rows.forEachIndexed { ri, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (ri % 2 == 1) Color(0xFF141418) else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                row.forEachIndexed { i, cell ->
                    Text(
                        text = inlineMarkdown(cell, TextPrimary),
                        color = TextPrimary,
                        fontSize = cellSize,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = if (i < row.size - 1) 8.dp else 0.dp),
                    )
                }
            }
            if (ri < rows.size - 1) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.1f)))
            }
        }
    }
}

private fun highlightSyntax(code: String, lang: String): AnnotatedString {
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
                withStyle(SpanStyle(color = CodeColors.Comment, fontStyle = FontStyle.Italic)) {
                    append(code.substring(i, end))
                }
                i = end
                continue
            }

            // Block comment
            if (i + 1 < code.length && code[i] == '/' && code[i + 1] == '*') {
                val end = code.indexOf("*/", i + 2).let { if (it == -1) code.length else it + 2 }
                withStyle(SpanStyle(color = CodeColors.Comment, fontStyle = FontStyle.Italic)) {
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
                withStyle(SpanStyle(color = CodeColors.String)) {
                    append(code.substring(i, j))
                }
                i = j
                continue
            }

            // Number
            if (c.isDigit()) {
                var j = i
                while (j < code.length && (code[j].isDigit() || code[j] == '.' || code[j] == '_')) j++
                withStyle(SpanStyle(color = CodeColors.Number)) {
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
                    keywords.contains(word) -> CodeColors.Keyword
                    types.contains(word) -> CodeColors.Type
                    j < code.length && code[j] == '(' -> CodeColors.Function
                    word.firstOrNull()?.isUpperCase() == true && word.length > 1 -> CodeColors.Type
                    else -> CodeColors.Default
                }
                withStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = if (keywords.contains(word)) FontWeight.Normal else FontWeight.Normal,
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
                    withStyle(SpanStyle(color = CodeColors.Operator)) { append(c) }
                }
                '(', ')', '[', ']', '{', '}', ',', ';', ':', '.' -> {
                    withStyle(SpanStyle(color = CodeColors.Default)) { append(c) }
                }
                else -> {
                    withStyle(SpanStyle(color = CodeColors.Default)) { append(c) }
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
    fontSize: Int = 15,
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
            .background(Color(0xFF0E0E12))
            .border(1.dp, Accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
    ) {
        // Header مع زر نسخ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14141A))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("\u2705", fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "\u0645\u062d\u062a\u0648\u0649 \u0645\u0646\u0638\u0645",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
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
                Text("\u0646\u0633\u062e", color = Accent, fontSize = 11.sp)
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
    fontSize: Int = 15,
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun isImportantReply(text: String): Boolean {
    if (text.length < 100) return false
    val hasHeading = Regex("(?m)^#{1,4}\\s").containsMatchIn(text)
    val hasTable = Regex("(?m)^\\|.+\\|").containsMatchIn(text)
    val hasQuote = Regex("(?m)^>\\s?\\[!").containsMatchIn(text)
    val numberedCount = Regex("(?m)^\\d{1,2}[.)]\\s").findAll(text).count()
    val bulletCount = Regex("(?m)^[-*]\\s").findAll(text).count()
    return hasHeading || hasTable || hasQuote || numberedCount >= 3 || bulletCount >= 3
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

private fun inlineMarkdown(textRaw: String, baseColor: Color): AnnotatedString {
    val text = beautifyMath(textRaw)
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end > 0) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            if (i + 1 < text.length && text[i] == '_' && text[i + 1] == '_') {
                val end = text.indexOf("__", i + 2)
                if (end > 0) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
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
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end > i + 1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFD8B4FE),
                            background = Color(0xFF1E1B2E),
                            fontSize = (13).sp,
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
