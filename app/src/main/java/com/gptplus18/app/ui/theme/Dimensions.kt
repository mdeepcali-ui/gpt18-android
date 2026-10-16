package com.gptplus18.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ═══════════════════════════════════════════════════════════════
 *  Dimensions — Spacing System
 *  UXRD-2026-10-16-001 · Phase 1 · D-1.2
 * ═══════════════════════════════════════════════════════════════
 *
 *  نظام المسافات المركزي — 4-based scale.
 *
 *  المبادئ:
 *    • كل مسافة في التطبيق تأتي من هنا
 *    • لا أرقام عشوائية (لا 7.dp، لا 13.dp، لا 17.dp)
 *    • أسماء دلالية (semantic) لكل عنصر
 *
 *  الأساس:
 *    XXS = 2   · للفروقات الدقيقة جداً
 *    XS  = 4   · بين أيقونة ونصها
 *    S   = 8   · padding صغير
 *    M   = 12  · padding افتراضي
 *    L   = 16  · بين العناصر
 *    XL  = 24  · بين الأقسام
 *    XXL = 32  · بين مجموعات كبيرة
 *    XXXL= 48  · أعلى/أسفل الشاشة
 *
 *  الاستخدام:
 *    val dim = LocalDimensions.current
 *    Modifier.padding(dim.screenPadding, dim.m)
 */

data class Dimensions(
    // ═══════ SCALE الأساسي ═══════
    val xxs: Dp = 2.dp,
    val xs:  Dp = 4.dp,
    val s:   Dp = 8.dp,
    val m:   Dp = 12.dp,
    val l:   Dp = 16.dp,
    val xl:  Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl:Dp = 48.dp,

    // ═══════ SHAPES (للتوافق السريع) ═══════
    val none: Dp = 0.dp,

    // ═══════ SCREEN ═══════
    val screenPadding:    Dp = 16.dp,   // الحافة الأفقية للشاشة
    val screenPaddingV:   Dp = 12.dp,   // الحافة الرأسية
    val contentMaxWidth:  Dp = 720.dp,  // للـ tablets

    // ═══════ APP BAR ═══════
    val topBarHeight:     Dp = 56.dp,
    val topBarIconSize:   Dp = 24.dp,
    val topBarPadding:    Dp = 8.dp,

    // ═══════ BOTTOM BAR / COMPOSER ═══════
    val bottomBarHeight:  Dp = 64.dp,
    val composerPadding:  Dp = 12.dp,
    val composerMinHeight:Dp = 48.dp,
    val composerMaxHeight:Dp = 200.dp,
    val composerRadius:   Dp = 24.dp,

    // ═══════ CHAT BUBBLES ═══════
    val bubblePaddingH:   Dp = 14.dp,   // أفقي
    val bubblePaddingV:   Dp = 10.dp,   // رأسي
    val bubbleMaxWidth:   Dp = 320.dp,  // أقصى عرض
    val bubbleGapV:       Dp = 8.dp,    // بين فقاعات (رأسي)
    val bubbleGapH:       Dp = 4.dp,    // بين مجموعات
    val bubbleCorner:     Dp = 18.dp,   // نصف قطر الزاوية
    val bubbleTailCorner: Dp = 4.dp,    // الزاوية الحادة

    // ═══════ LIST / ITEMS ═══════
    val listItemHeight:   Dp = 56.dp,
    val listItemPaddingH: Dp = 16.dp,
    val listItemPaddingV: Dp = 12.dp,
    val listItemGap:      Dp = 4.dp,
    val listSectionGap:   Dp = 24.dp,

    // ═══════ CARDS ═══════
    val cardPadding:      Dp = 16.dp,
    val cardPaddingS:     Dp = 12.dp,
    val cardGapV:         Dp = 12.dp,
    val cardGapH:         Dp = 12.dp,
    val cardRadius:       Dp = 16.dp,

    // ═══════ ICONS ═══════
    val iconXS:  Dp = 14.dp,
    val iconS:   Dp = 16.dp,
    val iconM:   Dp = 20.dp,
    val iconL:   Dp = 24.dp,
    val iconXL:  Dp = 32.dp,
    val iconXXL: Dp = 48.dp,
    val iconHuge:Dp = 80.dp,  // للـ Empty States

    // ═══════ BUTTONS ═══════
    val buttonHeightL:    Dp = 48.dp,   // رئيسي
    val buttonHeightM:    Dp = 40.dp,   // ثانوي
    val buttonHeightS:    Dp = 32.dp,   // صغير
    val iconButtonSize:   Dp = 40.dp,
    val iconButtonSizeS:  Dp = 32.dp,
    val iconButtonSizeL:  Dp = 48.dp,
    val fabSize:          Dp = 56.dp,
    val fabSizeSmall:     Dp = 40.dp,
    val buttonMinWidth:   Dp = 64.dp,
    val buttonPaddingH:   Dp = 20.dp,
    val buttonPaddingV:   Dp = 10.dp,

    // ═══════ AVATARS ═══════
    val avatarS:  Dp = 32.dp,
    val avatarM:  Dp = 40.dp,
    val avatarL:  Dp = 56.dp,
    val avatarXL: Dp = 80.dp,

    // ═══════ DIVIDERS ═══════
    val dividerThickness: Dp = 1.dp,
    val dividerIndent:    Dp = 16.dp,

    // ═══════ STATUS / BADGES ═══════
    val statusDotSize:    Dp = 8.dp,
    val badgeMinSize:     Dp = 20.dp,
    val badgePaddingH:    Dp = 8.dp,
    val badgePaddingV:    Dp = 2.dp,

    // ═══════ DIALOGS / SHEETS ═══════
    val dialogMaxWidth:   Dp = 360.dp,
    val dialogPadding:    Dp = 24.dp,
    val dialogGap:        Dp = 16.dp,
    val sheetHandleWidth: Dp = 32.dp,
    val sheetHandleHeight:Dp = 4.dp,
    val sheetHandlePadV:  Dp = 12.dp,

    // ═══════ THINKING BOX ═══════
    val thinkingPadding:  Dp = 12.dp,
    val thinkingLineGap:  Dp = 6.dp,
    val thinkingMaxHeight:Dp = 400.dp,

    // ═══════ CODE BLOCK ═══════
    val codePadding:      Dp = 12.dp,
    val codeLineGap:      Dp = 2.dp,
    val codeRadius:       Dp = 10.dp,
    val codeHeaderHeight: Dp = 36.dp,

    // ═══════ EMPTY STATES ═══════
    val emptyIconSize:    Dp = 80.dp,
    val emptyGapTop:      Dp = 48.dp,
    val emptyGapIconText: Dp = 24.dp,
    val emptyGapTextBtn:  Dp = 32.dp,

    // ═══════ ANIMATION PRESET ═══════
    val staggerStep:      Dp = 20.dp,   // مستخدم في animations بصرية
)

// ═══════════════════════════════════════════════════════════════
//  CompositionLocal — للوصول من أي Composable
// ═══════════════════════════════════════════════════════════════
val LocalDimensions = staticCompositionLocalOf { Dimensions() }

// ═══════════════════════════════════════════════════════════════
//  Object ثابت — للاستخدام خارج @Composable
// ═══════════════════════════════════════════════════════════════
object AppDimensions {
    val dimensions: Dimensions = Dimensions()

    // الاختصارات الشائعة
    val xxs get() = dimensions.xxs
    val xs  get() = dimensions.xs
    val s   get() = dimensions.s
    val m   get() = dimensions.m
    val l   get() = dimensions.l
    val xl  get() = dimensions.xl
    val xxl get() = dimensions.xxl
    val xxxl get() = dimensions.xxxl
    val screen get() = dimensions.screenPadding
}
