package com.gptplus18.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * ═══════════════════════════════════════════════════════════════
 *  Shapes — Rounded Corner System
 *  UXRD-2026-10-16-001 · Phase 1 · D-1.3
 * ═══════════════════════════════════════════════════════════════
 *
 *  نظام الأشكال المركزي.
 *
 *  المبادئ:
 *    • لا RoundedCornerShape(7.dp) بعد اليوم
 *    • كل شكل يُعرَّف هنا مرة واحدة
 *    • Material 3 Shapes + إضافات وظيفية
 *
 *  الأساس:
 *    XS  = 4   · chips صغيرة
 *    S   = 8   · أزرار صغيرة
 *    M   = 12  · كاردات
 *    L   = 16  · dialogs
 *    XL  = 22  · bottom sheets
 *    XXL = 28  · كاردات ضخمة
 *    Circle    · أفاتار، FAB
 *
 *  الاستخدام:
 *    val shapes = AppShapes
 *    Surface(shape = shapes.large) { ... }
 *    Modifier.clip(AppShapes.m)
 */

// ═══════════════════════════════════════════════════════════════
//  Material 3 Shapes — للاستخدام مع MaterialTheme
// ═══════════════════════════════════════════════════════════════
val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),    // chips, tags
    small      = RoundedCornerShape(8.dp),    // أزرار صغيرة
    medium     = RoundedCornerShape(12.dp),   // كاردات
    large      = RoundedCornerShape(16.dp),   // dialogs
    extraLarge = RoundedCornerShape(22.dp),   // bottom sheets
)

// ═══════════════════════════════════════════════════════════════
//  AppShapes — النظام الكامل
// ═══════════════════════════════════════════════════════════════
object AppShapes {

    // ═══════ SCALE الأساسي ═══════
    val none  = RoundedCornerShape(0.dp)
    val xs    = RoundedCornerShape(4.dp)
    val s     = RoundedCornerShape(8.dp)
    val m     = RoundedCornerShape(12.dp)
    val l     = RoundedCornerShape(16.dp)
    val xl    = RoundedCornerShape(22.dp)
    val xxl   = RoundedCornerShape(28.dp)
    val circle = CircleShape

    // ═══════ CHAT BUBBLES ═══════
    /** فقاعة المستخدم — الزاوية السفلية اليمنى حادة */
    val bubbleUser = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 4.dp,
        bottomStart = 18.dp,
    )

    /** فقاعة المساعد — الزاوية السفلية اليسرى حادة */
    val bubbleAssistant = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 18.dp,
        bottomStart = 4.dp,
    )

    /** فقاعة موحّدة — كل الزوايا نفسها */
    val bubbleUniform = RoundedCornerShape(18.dp)

    /** فقاعة داخلية (اقتباس، status) */
    val bubbleNested = RoundedCornerShape(12.dp)

    // ═══════ COMPOSER (صندوق الكتابة) ═══════
    val composer       = RoundedCornerShape(24.dp)
    val composerInner  = RoundedCornerShape(20.dp)
    val composerSmall  = RoundedCornerShape(16.dp)

    // ═══════ CODE BLOCKS ═══════
    val codeBlock      = RoundedCornerShape(10.dp)
    val codeBlockSmall = RoundedCornerShape(8.dp)
    val inlineCode     = RoundedCornerShape(6.dp)

    // ═══════ BUTTONS ═══════
    val button       = RoundedCornerShape(12.dp)
    val buttonSmall  = RoundedCornerShape(8.dp)
    val buttonLarge  = RoundedCornerShape(16.dp)
    val buttonPill   = RoundedCornerShape(percent = 50)

    // ═══════ CARDS ═══════
    val card        = RoundedCornerShape(16.dp)
    val cardSmall   = RoundedCornerShape(12.dp)
    val cardLarge   = RoundedCornerShape(20.dp)

    // ═══════ DIALOGS & SHEETS ═══════
    val dialog       = RoundedCornerShape(24.dp)
    val dialogSmall  = RoundedCornerShape(20.dp)
    val sheet        = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )
    val bottomSheetFull = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )

    // ═══════ MENUS / DROPDOWNS ═══════
    val dropdown     = RoundedCornerShape(12.dp)
    val dropdownItem = RoundedCornerShape(8.dp)

    // ═══════ TOOLTIPS / BADGES ═══════
    val tooltip   = RoundedCornerShape(6.dp)
    val badge     = RoundedCornerShape(percent = 50)
    val tag       = RoundedCornerShape(percent = 50)

    // ═══════ THINKING BOX ═══════
    val thinkingBox     = RoundedCornerShape(12.dp)
    val thinkingBoxItem = RoundedCornerShape(8.dp)

    // ═══════ INPUT FIELDS ═══════
    val inputField     = RoundedCornerShape(12.dp)
    val inputFieldTop  = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )
    val searchBar      = RoundedCornerShape(24.dp)

    // ═══════ IMAGES / MEDIA ═══════
    val imageSmall     = RoundedCornerShape(8.dp)
    val imageMedium    = RoundedCornerShape(12.dp)
    val imageLarge     = RoundedCornerShape(16.dp)
    val attachment     = RoundedCornerShape(10.dp)

    // ═══════ HELPER: زاوية واحدة فقط ═══════
    fun topRounded(radius: Int = 16) = RoundedCornerShape(
        topStart = radius.dp,
        topEnd = radius.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )

    fun bottomRounded(radius: Int = 16) = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = radius.dp,
        bottomEnd = radius.dp,
    )

    fun startRounded(radius: Int = 16) = RoundedCornerShape(
        topStart = radius.dp,
        topEnd = 0.dp,
        bottomStart = radius.dp,
        bottomEnd = 0.dp,
    )

    fun endRounded(radius: Int = 16) = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = radius.dp,
        bottomStart = 0.dp,
        bottomEnd = radius.dp,
    )
}

// ═══════════════════════════════════════════════════════════════
//  CompositionLocal — للوصول من @Composable إذا احتجنا
// ═══════════════════════════════════════════════════════════════
val LocalShapes = staticCompositionLocalOf { AppShapes }
