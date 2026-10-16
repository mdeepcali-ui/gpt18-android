package com.gptplus18.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ═══════════════════════════════════════════════════════════════
 *  Elevation — Shadow System
 *  UXRD-2026-10-16-001 · Phase 1 · D-1.4
 * ═══════════════════════════════════════════════════════════════
 *
 *  نظام الظلال المركزي — 5 مستويات.
 *
 *  المبادئ:
 *    • لا Modifier.shadow(7.dp) بعد اليوم
 *    • كل ظل في التطبيق يأتي من هنا
 *    • تدرّج منطقي (flat → floating)
 *
 *  المستويات:
 *    Level0 (0dp)  · flat      — بدون ظل، للأسطح الأساسية
 *    Level1 (1dp)  · subtle    — كاردات صغيرة، أزرار
 *    Level2 (3dp)  · raised    — FAB، عناصر طافية
 *    Level3 (6dp)  · modal     — dialogs، dropdowns
 *    Level4 (12dp) · floating  — bottom sheets، منبثقات كبيرة
 *
 *  الاستخدام:
 *    val elev = LocalElevation.current
 *    Modifier.shadow(elev.card)
 *    Card(elevation = CardDefaults.cardElevation(elev.raised)) { ... }
 */

data class Elevation(
    // ═══════ الأساسي ═══════
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 12.dp,
) {
    // ═══════ أسماء دلالية ═══════
    /** بدون ظل — أسطح مسطحة (bottom bars, composer) */
    val flat: Dp get() = level0

    /** ظل خفيف — كاردات صغيرة، أزرار عادية */
    val subtle: Dp get() = level1

    /** ظل ملحوظ — FAB، عناصر طافية */
    val raised: Dp get() = level2

    /** ظل متوسط — dialogs، dropdowns، tooltips */
    val modal: Dp get() = level3

    /** ظل قوي — bottom sheets، منبثقات كاملة */
    val floating: Dp get() = level4

    // ═══════ aliases للاستخدام السريع ═══════
    val none: Dp get() = level0
    val card: Dp get() = level1
    val cardRaised: Dp get() = level2
    val button: Dp get() = level1
    val buttonPressed: Dp get() = level0
    val fab: Dp get() = level2
    val fabPressed: Dp get() = level1
    val appBar: Dp get() = level1
    val appBarElevated: Dp get() = level2
    val bottomBar: Dp get() = level1
    val dialog: Dp get() = level3
    val sheet: Dp get() = level4
    val dropdown: Dp get() = level3
    val tooltip: Dp get() = level3
    val bubble: Dp get() = level0
    val composer: Dp get() = level1
    val image: Dp get() = level1
    val imageFullscreen: Dp get() = level4
    val snackbar: Dp get() = level3
    val thinkingBox: Dp get() = level0
    val codeBlock: Dp get() = level0
}

// ═══════════════════════════════════════════════════════════════
//  CompositionLocal — للوصول من @Composable
// ═══════════════════════════════════════════════════════════════
val LocalElevation = staticCompositionLocalOf { Elevation() }

// ═══════════════════════════════════════════════════════════════
//  AppElevation — object للاستخدام المباشر خارج @Composable
// ═══════════════════════════════════════════════════════════════
object AppElevation {
    val elevation = Elevation()

    // الاختصارات الشائعة
    val flat      get() = elevation.flat
    val subtle    get() = elevation.subtle
    val raised    get() = elevation.raised
    val modal     get() = elevation.modal
    val floating  get() = elevation.floating
    val none      get() = elevation.none
    val card      get() = elevation.card
    val fab       get() = elevation.fab
    val dialog    get() = elevation.dialog
    val sheet     get() = elevation.sheet
    val appBar    get() = elevation.appBar
    val bottomBar get() = elevation.bottomBar
}
