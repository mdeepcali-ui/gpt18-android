package com.gptplus18.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gptplus18.app.R

// ═══════════════════════════════════════════
// Rubik Font Family
// ═══════════════════════════════════════════
val RubikFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_semibold, FontWeight.SemiBold),
    Font(R.font.rubik_bold, FontWeight.Bold),
)

val AppTypography = Typography(
    // ─── العناوين ───
    displayLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    // ─── العناوين الفرعية ───
    titleLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    // ─── النصوص الأساسية ───
    bodyLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.2.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // ─── الأزرار والعلامات ───
    labelLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)
