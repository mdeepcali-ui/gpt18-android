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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════
// App Colors — يدعم Light/Dark
// ═══════════════════════════════════════════
data class AppColors(
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentContainer: Color,
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
    isLight = true,
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

// ─── Schemes لـ Material3 ───
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

@Composable
fun GptPlus18Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val scheme = if (darkTheme) DarkScheme else LightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.bg.toArgb()
            window.navigationBarColor = appColors.bg.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content,
        )
    }
}
