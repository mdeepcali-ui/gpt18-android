package com.gptplus18.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * 🎨 علامات الكلمات الحساسة
 * النموذج يلف الكلمات الحساسة بـ [[حساس]]...[[/حساس]]
 * هذه الدالة تحولها إلى نص ملون
 */
object SensitiveMarkers {

    private const val OPEN = "[[حساس]]"
    private const val CLOSE = "[[/حساس]]"

    // 🔴 لون الكلمات الحساسة
    val HIGHLIGHT_COLOR = Color(0xFFFF6B6B)

    fun apply(text: String, baseColor: Color): AnnotatedString {
        // إذا ما في علامات — نص عادي
        if (!text.contains(OPEN)) {
            return buildAnnotatedString {
                withStyle(SpanStyle(color = baseColor)) { append(text) }
            }
        }

        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                val start = text.indexOf(OPEN, i)

                if (start < 0) {
                    // ما في علامات بعد i
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

                val contentStart = start + OPEN.length
                val end = text.indexOf(CLOSE, contentStart)

                if (end < 0) {
                    // ما في إغلاق
                    withStyle(SpanStyle(color = baseColor)) {
                        append(text.substring(start))
                    }
                    break
                }

                // الكلمة الحساسة → ملونة
                val sensitive = text.substring(contentStart, end)
                withStyle(
                    SpanStyle(
                        color = HIGHLIGHT_COLOR,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append(sensitive)
                }

                i = end + CLOSE.length
            }
        }
    }
}
