package com.gptplus18.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

/**
 * 🎨 علامات الكلمات الحساسة — 3 ألوان
 * - [[حساس_احمر]]...[[/حساس_احمر]]       🔴
 * - [[حساس_برتقالي]]...[[/حساس_برتقالي]] 🟠
 * - [[حساس_اخضر]]...[[/حساس_اخضر]]       🟢
 */
object SensitiveMarkers {

    // ─── الألوان ───
    val RED = Color(0xFFFF6B6B)
    val ORANGE = Color(0xFFFFA657)
    val GREEN = Color(0xFF7EE787)

    private data class Marker(
        val open: String,
        val close: String,
        val color: Color,
    )

    private val MARKERS = listOf(
        Marker("[[حساس_احمر]]", "[[/حساس_احمر]]", RED),
        Marker("[[حساس_برتقالي]]", "[[/حساس_برتقالي]]", ORANGE),
        Marker("[[حساس_اخضر]]", "[[/حساس_اخضر]]", GREEN),
    )

    fun apply(text: String, baseColor: Color): AnnotatedString {
        // نتحقق إذا في أي علامة
        if (MARKERS.none { text.contains(it.open) }) {
            return buildAnnotatedString {
                withStyle(SpanStyle(color = baseColor)) { append(text) }
            }
        }

        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                // نبحث عن أقرب علامة
                var nearest: Pair<Int, Marker>? = null
                for (m in MARKERS) {
                    val idx = text.indexOf(m.open, i)
                    if (idx >= 0 && (nearest == null || idx < nearest!!.first)) {
                        nearest = idx to m
                    }
                }

                if (nearest == null) {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(text.substring(i))
                    }
                    break
                }

                val (start, marker) = nearest!!
                val contentStart = start + marker.open.length
                val end = text.indexOf(marker.close, contentStart)

                if (end < 0) {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(text.substring(i))
                    }
                    break
                }

                // نص عادي قبل العلامة
                if (start > i) {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(text.substring(i, start))
                    }
                }

                // الكلمة الحساسة → لون
                val sensitive = text.substring(contentStart, end)
                withStyle(
                    SpanStyle(
                        color = marker.color,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append(sensitive)
                }

                i = end + marker.close.length
            }
        }
    }
}
