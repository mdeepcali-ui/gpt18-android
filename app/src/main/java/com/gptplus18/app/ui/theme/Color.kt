package com.gptplus18.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════
// 🌙 Dark Theme — أسود قاتم + أبيض
// ═══════════════════════════════════════════
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF0D0D0D)
val DarkSurfaceVariant = Color(0xFF1A1A1A)

val DarkTextPrimary = Color(0xFFE8E8EE)   // فضي متوهج
val DarkTextSecondary = Color(0xFFB8B8C0) // فضي
val DarkTextTertiary = Color(0xFF8888A0)  // فضي داكن

val DarkAccent = Color(0xFFFFFFFF)
val DarkAccentContainer = Color(0xFFE0E0E0)

// ═══════════════════════════════════════════
// ☀️ Light Theme
// ═══════════════════════════════════════════
val LightBg = Color(0xFFF7F7F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEAEAEF)

val LightTextPrimary = Color(0xFF2A2A30)
val LightTextSecondary = Color(0xFF606068)
val LightTextTertiary = Color(0xFF90909A)

val LightAccent = Color(0xFF1A1A1C)
val LightAccentContainer = Color(0xFF3A3A3C)

// ═══════════════════════════════════════════
// Semantic (ثابتة)
// ═══════════════════════════════════════════
val Success = Color(0xFF7EE787)
val Warning = Color(0xFFFFC58F)
val Error = Color(0xFFFF6B6B)
val Info = Color(0xFF93E0FF)

// ═══════════════════════════════════════════
// 🎨 Legacy names — ديناميكية (تتبع الوضع الحالي)
// استخدمها داخل @Composable فقط
// ═══════════════════════════════════════════
val BgPrimary: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.bg

val BgSecondary: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.surface

val BgTertiary: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.surfaceVariant

val TextPrimary: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.textPrimary

val TextSecondary: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

val TextTertiary: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.textTertiary

val Accent: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.accent

val AccentDark: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.accentContainer

// ═══════════════════════════════════════════
// 🎨 ألوان الفقاعات والـ Composer (تتبع الوضع)
// ═══════════════════════════════════════════
val BubbleBg: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.bubbleBg

val BubbleBorder: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.bubbleBorder

val ComposerBgColor: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.composerBg

val ComposerBorderColor: Color
    @Composable @ReadOnlyComposable
    get() = LocalAppColors.current.composerBorder
