package com.gptplus18.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = BgPrimary,
    primaryContainer = AccentDark,
    onPrimaryContainer = TextPrimary,
    secondary = AccentDark,
    onSecondary = TextPrimary,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = BgTertiary,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextPrimary,
)

private val LightColors = lightColorScheme(
    primary = AccentDark,
    onPrimary = LightSurface,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    error = Error,
)

@Composable
fun GptPlus18Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
