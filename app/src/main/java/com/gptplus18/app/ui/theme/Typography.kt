package com.gptplus18.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.gptplus18.app.R

/**
 * ═══════════════════════════════════════════════════════════════
 *  Typography — Material 3 Type Scale
 *  UXRD-2026-10-16-001 · Phase 1 · D-1.1
 * ═══════════════════════════════════════════════════════════════
 *
 *  نظام الطباعة المركزي للتطبيق كامل.
 *
 *  المبادئ:
 *    • 15 مستوى Material 3 رسمي — لا أرقام عشوائية
 *    • خط Lateef للعربية + اللاتينية (OFL License)
 *    • lineHeight × 1.6 للعربية (RTL-optimized)
 *    • letterSpacing = 0 للعربية، 0.15 للاتينية
 *    • أوزان صحيحة: Regular(400), Medium(500), SemiBold(600), Bold(700)
 *
 *  الاستخدام:
 *    Text("...", style = MaterialTheme.typography.bodyLarge)
 *    Text("...", style = AppTypography.headlineSmall)
 */

// ─────────────────────────────────────────────────────────────
//  Font Family — Lateef
// ─────────────────────────────────────────────────────────────
val LateefFontFamily = FontFamily(
    Font(R.font.lateef_regular,  FontWeight.Normal),    // 400
    Font(R.font.lateef_medium,   FontWeight.Medium),    // 500
    Font(R.font.lateef_semibold, FontWeight.SemiBold),  // 600
    Font(R.font.lateef_bold,     FontWeight.Bold),      // 700
)

// ─────────────────────────────────────────────────────────────
//  Line Height Style — مناسب للعربية
// ─────────────────────────────────────────────────────────────
private val ArabicLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

// ─────────────────────────────────────────────────────────────
//  Helper: بناء TextStyle موحّد
// ─────────────────────────────────────────────────────────────
private fun appStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f,
): TextStyle = TextStyle(
    fontFamily = LateefFontFamily,
    fontWeight = weight,
    fontSize = (size + 5).sp,
    lineHeight = (lineHeight + 8).sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = ArabicLineHeightStyle,
    textAlign = TextAlign.Start,
)

// ─────────────────────────────────────────────────────────────
//  Material 3 Type Scale — 15 مستويات
// ─────────────────────────────────────────────────────────────
val AppTypography = Typography(

    // ═══════ DISPLAY ═══════  (شاشات ترحيب / Onboarding)
    displayLarge = appStyle(
        weight = FontWeight.Bold,
        size = 57,
        lineHeight = 64,
    ),
    displayMedium = appStyle(
        weight = FontWeight.Bold,
        size = 45,
        lineHeight = 52,
    ),
    displaySmall = appStyle(
        weight = FontWeight.Bold,
        size = 36,
        lineHeight = 44,
    ),

    // ═══════ HEADLINE ═══════  (عناوين رئيسية)
    headlineLarge = appStyle(
        weight = FontWeight.Bold,
        size = 32,
        lineHeight = 40,
    ),
    headlineMedium = appStyle(
        weight = FontWeight.Bold,
        size = 28,
        lineHeight = 36,
    ),
    headlineSmall = appStyle(
        weight = FontWeight.Bold,
        size = 24,
        lineHeight = 32,
    ),

    // ═══════ TITLE ═══════  (AppBar, عناوين ثانوية)
    titleLarge = appStyle(
        weight = FontWeight.Bold,
        size = 22,
        lineHeight = 28,
    ),
    titleMedium = appStyle(
        weight = FontWeight.SemiBold,
        size = 16,
        lineHeight = 24,
        letterSpacing = 0.15f,
    ),
    titleSmall = appStyle(
        weight = FontWeight.SemiBold,
        size = 14,
        lineHeight = 20,
        letterSpacing = 0.1f,
    ),

    // ═══════ BODY ═══════  (النصوص الأساسية)
    bodyLarge = appStyle(
        weight = FontWeight.Medium,
        size = 16,
        lineHeight = 24,
        letterSpacing = 0.5f,
    ),
    bodyMedium = appStyle(
        weight = FontWeight.Medium,
        size = 14,
        lineHeight = 20,
        letterSpacing = 0.25f,
    ),
    bodySmall = appStyle(
        weight = FontWeight.Medium,
        size = 12,
        lineHeight = 16,
        letterSpacing = 0.4f,
    ),

    // ═══════ LABEL ═══════  (أزرار، chips, tags)
    labelLarge = appStyle(
        weight = FontWeight.SemiBold,
        size = 14,
        lineHeight = 20,
        letterSpacing = 0.1f,
    ),
    labelMedium = appStyle(
        weight = FontWeight.SemiBold,
        size = 12,
        lineHeight = 16,
        letterSpacing = 0.5f,
    ),
    labelSmall = appStyle(
        weight = FontWeight.SemiBold,
        size = 11,
        lineHeight = 16,
        letterSpacing = 0.5f,
    ),
)

// ─────────────────────────────────────────────────────────────
//  Code Typography — Monospace للأكواد
// ─────────────────────────────────────────────────────────────
val CodeTypography = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
)

val CodeTypographySmall = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
)
