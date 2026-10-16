package com.gptplus18.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * ═══════════════════════════════════════════════════════════════
 *  Theme — Central Theme Provider
 *  UXRD-2026-10-16-001 · Phase 1
 * ═══════════════════════════════════════════════════════════════
 *
 *  المزود المركزي للثيم — يربط كل أنظمة التصميم:
 *    • AppColors        → LocalAppColors    (Light/Dark)
 *    • AppTypography    → MaterialTheme.typography
 *    • M3Shapes         → MaterialTheme.shapes
 *    • Dimensions       → LocalDimensions
 *    • AppShapes        → LocalShapes
 *    • Elevation        → LocalElevation
 *    • Motion           → LocalMotion
 *
 *  الاستخدام في Activity:
 *    GptPlus18Theme { ... }
 */

// ═══════════════════════════════════════════════════════════════
//  App Colors — يدعم Light/Dark
// ═══════════════════════════════════════════════════════════════
data class AppColors(
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentContainer: Color,
    val bubbleBg: Color,
    val bubbleBorder: Color,
    val composerBg: Color,
    val composerBorder: Color,
    val isLight: Boolean,
)

val DarkAppColors = AppColors(
    bg = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    accent = DarkAccent,
    accentContainer = DarkAccentContainer,
    bubbleBg = Color(0xFF333333),
    bubbleBorder = Color(0xFF3A3F47),
    composerBg = Color(0xFF333333),
    composerBorder = Color(0xFF3D3D3D),
    isLight = false,
)

val LightAppColors = AppColors(
    bg = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    accent = LightAccent,
    accentContainer = LightAccentContainer,
    bubbleBg = Color(0xFFEBEDF0),
    bubbleBorder = Color(0xFFD0D3D8),
    composerBg = Color(0xFFFFFFFF),
    composerBorder = Color(0xFFD5D5DD),
    isLight = true,
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

// ═══════════════════════════════════════════════════════════════
//  Material 3 Color Schemes
// ═══════════════════════════════════════════════════════════════
private val DarkScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBg,
    primaryContainer = DarkAccentContainer,
    onPrimaryContainer = DarkTextPrimary,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    error = Error,
    onError = Color.White,
)

private val LightScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    error = Error,
    onError = Color.White,
)

// ═══════════════════════════════════════════════════════════════
//  Theme Composable
// ═══════════════════════════════════════════════════════════════
@Composable
fun GptPlus18Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val scheme = if (darkTheme) DarkScheme else LightScheme

    // ── Edge-to-Edge: ضبط لون أيقونات شريط الحالة ──
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // ── نُثبّت كل الأنظمة في CompositionLocals ──
    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalDimensions provides Dimensions(),
        LocalShapes provides AppShapes,
        LocalElevation provides Elevation(),
        LocalMotion provides Motion(),
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,   // ← Cairo + 15 levels
            shapes = M3Shapes,             // ← M3 compatible
            content = content,
        )
    }
}
