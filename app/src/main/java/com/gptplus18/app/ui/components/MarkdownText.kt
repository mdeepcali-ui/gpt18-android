package com.gptplus18.app.ui.components

import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
                    Text(
                        text = inlineMarkdown(block.content, textColor, fontSize),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = size.sp,
                        lineHeight = (size + 8).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = topPad, bottom = 6.dp),
                    )
                }
                is MdBlock.Bullet -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "●",
                            color = Accent,
                            fontSize = (fontSize - 4).sp,
                            modifier = Modifier.padding(top = 5.dp, end = 10.dp),
                        )
                        Text(
                            text = inlineMarkdown(block.content, textColor, fontSize),
                            color = textColor,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 9).sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.Numbered -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, top = 6.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = block.num + ".",
                            color = Accent,
                            fontSize = (fontSize + 1).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        Text(
                            text = inlineMarkdown(block.content, textColor, fontSize),
                            color = textColor,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 9).sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.Quote -> {
                    // ⭐ اقتباس: فقاعة سوداء + إطار رمادي + خط أصغر
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, start = 4.dp, end = 4.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F0F12))
                            .border(
                                width = 1.dp,
                                color = TextSecondary.copy(alpha = 0.35f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = inlineMarkdown(block.content, TextSecondary, fontSize - 1),
                            color = TextSecondary,
                            fontSize = (fontSize - 1).sp,
                            lineHeight = (fontSize + 6).sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
                is MdBlock.Paragraph -> Text(
                    text = inlineMarkdown(block.content, textColor, fontSize),
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
                val end = code.indexOf('\import androidx.compose.foundation.border\nimport androidx.compose.foundation.layout.Box\nn', i).let { if (it == -1) code.length else it }
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

private sealed class MdBlock {
    data class Paragraph(val content: String) : MdBlock()
    data class CodeBlock(val lang: String, val content: String) : MdBlock()
    data class Heading(val level: Int, val content: String) : MdBlock()
    data class Numbered(val num: String, val content: String) : MdBlock()
    data class Bullet(val content: String) : MdBlock()
    data class Image(val alt: String, val url: String) : MdBlock()
    data class Quote(val content: String) : MdBlock()
}

private fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    val lines = raw.split("\n")
    val blocks = mutableListOf<MdBlock>()
    var inCode = false
    var codeLang = ""
    val codeBuf = StringBuilder()
    val paraBuf = StringBuilder()

    fun flushPara() {
        if (paraBuf.isNotBlank()) {
            val rawPara = paraBuf.toString().trimEnd()
            val imgRegex = Regex("""!\[([^\]]*)\]\(([^)]+)\)""")
            val imgMatches = imgRegex.findAll(rawPara).toList()
            if (imgMatches.isNotEmpty()) {
                var lastIdx = 0
                for (m in imgMatches) {
                    val before = rawPara.substring(lastIdx, m.range.first).trim()
                    if (before.isNotBlank()) blocks.add(MdBlock.Paragraph(before))
                    val alt = m.groupValues.getOrNull(1) ?: ""
                    val url = m.groupValues.getOrNull(2) ?: ""
                    if (url.isNotBlank()) blocks.add(MdBlock.Image(alt, url))
                    lastIdx = m.range.last + 1
                }
                val after = rawPara.substring(lastIdx).trim()
                if (after.isNotBlank()) blocks.add(MdBlock.Paragraph(after))
            } else {
                blocks.add(MdBlock.Paragraph(rawPara))
            }
            paraBuf.clear()
        }
    }

    val headingRegex = Regex("^(#{1,4})\\s+(.+)$")
    val numberedRegex = Regex("^(\\d{1,2})[.)]\\s+(.+)$")

    lines.forEach { line ->
        val trimmed = line.trimStart()
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
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                flushPara()
                blocks.add(MdBlock.Bullet(trimmed.substring(2).trim()))
            }
            trimmed.startsWith("> ") -> {
                flushPara()
                blocks.add(MdBlock.Quote(trimmed.substring(2).trim()))
            }
            trimmed == ">" -> {
                flushPara()
                blocks.add(MdBlock.Quote(""))
            }
            line.isBlank() -> flushPara()
            else -> {
                if (paraBuf.isNotEmpty()) paraBuf.append("\n")
                paraBuf.append(line)
            }
        }
    }
    flushPara()
    if (inCode && codeBuf.isNotBlank()) {
        blocks.add(MdBlock.CodeBlock(codeLang, codeBuf.toString().trimEnd()))
    }
    return blocks
}

// ⭐ معالجة inline: **bold** و `code`
private fun inlineMarkdown(text: String, textColor: Color, fontSize: Int): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    val pattern = Regex("(\\*\\*[^*]+\\*\\*|`[^`]+`|__[^_]+__)")
    var last = 0
    for (m in pattern.findAll(text)) {
        if (m.range.first > last) {
            builder.append(text.substring(last, m.range.first))
        }
        val token = m.value
        when {
            token.startsWith("**") && token.endsWith("**") -> {
                val inner = token.substring(2, token.length - 2)
                builder.withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(inner)
                }
            }
            token.startsWith("__") && token.endsWith("__") -> {
                val inner = token.substring(2, token.length - 2)
                builder.withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(inner)
                }
            }
            token.startsWith("`") && token.endsWith("`") && token.length >= 2 -> {
                val inner = token.substring(1, token.length - 1)
                builder.withStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = (fontSize - 1).sp,
                        color = Accent,
                        background = Color(0xFF1A1A1E),
                    )
                ) {
                    append(" $inner ")
                }
            }
            else -> builder.append(token)
        }
        last = m.range.last + 1
    }
    if (last < text.length) {
        builder.append(text.substring(last))
    }
    return builder.toAnnotatedString()
}

private fun inlineMarkdown(text: String, baseColor: Color): AnnotatedString {
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
                            color = CodeColors.String,
                            background = CodeBlockColors.Background,
                        ),
                    ) {
                        append(text.substring(i + 1, end))
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
