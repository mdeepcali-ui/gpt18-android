package com.gptplus18.app.ui.components

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
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: Int = 17,
) {
    val blocks = parseMarkdownBlocks(text)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.CodeBlock -> CodeBlockView(block.lang, block.content)
                is MdBlock.Heading -> Text(
                    text = block.content,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = (fontSize + 3).sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                is MdBlock.Bullet -> Row(Modifier.fillMaxWidth()) {
                    Text("• ", color = Accent, fontSize = fontSize.sp)
                    Text(
                        text = SensitiveMarkers.apply(block.content, textColor),
                        fontSize = fontSize.sp,
                    )
                }
                is MdBlock.Paragraph -> Text(
                    text = SensitiveMarkers.apply(block.content, textColor),
                    color = textColor,
                    fontSize = fontSize.sp,
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

private sealed class MdBlock {
    data class Paragraph(val content: String) : MdBlock()
    data class CodeBlock(val lang: String, val content: String) : MdBlock()
    data class Heading(val content: String) : MdBlock()
    data class Bullet(val content: String) : MdBlock()
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
            blocks.add(MdBlock.Paragraph(paraBuf.toString().trimEnd()))
            paraBuf.clear()
        }
    }

    lines.forEach { line ->
        val trimmed = line.trimStart()
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
            trimmed.startsWith("# ") -> {
                flushPara()
                blocks.add(MdBlock.Heading(trimmed.substring(2)))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                flushPara()
                blocks.add(MdBlock.Bullet(trimmed.substring(2)))
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
