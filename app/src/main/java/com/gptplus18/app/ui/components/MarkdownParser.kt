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

internal sealed class MdBlock {
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

internal fun parseMarkdownBlocks(raw: String): List<MdBlock> {
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

internal fun inlineMarkdown(textRaw: String, baseColor: Color, isLight: Boolean = false): AnnotatedString {
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
