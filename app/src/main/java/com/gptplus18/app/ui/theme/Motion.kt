package com.gptplus18.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset

/**
 * ═══════════════════════════════════════════════════════════════
 *  Motion — Animation System
 *  UXRD-2026-10-16-001 · Phase 1 · D-1.5
 * ═══════════════════════════════════════════════════════════════
 *
 *  نظام الحركة المركزي — Spring-first.
 *
 *  المبادئ:
 *    • كل حركة في التطبيق spring (مو linear)
 *    • 4 مستويات سرعة: Fast, Medium, Slow, Bouncy
 *    • Haptic Feedback matrix موحّد
 *    • Staggered entry لإحساس احترافي
 *
 *  Spring Specs:
 *    FastSpring    → stiffness=800, damping=0.85  · للحركات الفورية
 *    MediumSpring  → stiffness=400, damping=0.85  · للحركات العادية
 *    SlowSpring    → stiffness=200, damping=0.90  · للحركات الكبيرة
 *    BouncySpring  → stiffness=400, damping=0.50  · للحركات المرحة
 *    SoftSpring    → stiffness=300, damping=0.95  · للحركات الناعمة
 *
 *  Durations (احتياطي عند الحاجة tween):
 *    Fast   = 150ms
 *    Medium = 250ms
 *    Slow   = 400ms
 *    ExtraSlow = 600ms
 *
 *  الاستخدام:
 *    val motion = LocalMotion.current
 *    Modifier.animateContentSize(motion.mediumSpring)
 *    animateFloatAsState(targetValue, animationSpec = motion.fastSpring)
 */

data class Motion(
    // ═══════ SPRING SPECS — Float ═══════
    val fastSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 800f,
    ),
    val mediumSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 400f,
    ),
    val slowSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.90f,
        stiffness = 200f,
    ),
    val bouncySpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.50f,
        stiffness = 400f,
    ),
    val softSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.95f,
        stiffness = 300f,
    ),

    // ═══════ SPRING SPECS — IntOffset ═══════
    val fastSpringOffset: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.85f,
        stiffness = 800f,
    ),
    val mediumSpringOffset: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.85f,
        stiffness = 400f,
    ),
    val slowSpringOffset: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.90f,
        stiffness = 200f,
    ),
    val bouncySpringOffset: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.50f,
        stiffness = 400f,
    ),

    // ═══════ SPRING SPECS — Generic (Dp, Color, etc.) ═══════
    val fastSpringGeneric: FiniteAnimationSpec<androidx.compose.ui.unit.Dp> = spring(
        dampingRatio = 0.85f,
        stiffness = 800f,
    ),
    val mediumSpringGeneric: FiniteAnimationSpec<androidx.compose.ui.unit.Dp> = spring(
        dampingRatio = 0.85f,
        stiffness = 400f,
    ),
    val slowSpringGeneric: FiniteAnimationSpec<androidx.compose.ui.unit.Dp> = spring(
        dampingRatio = 0.90f,
        stiffness = 200f,
    ),

    // ═══════ DURATIONS (ms) — للاستخدام في tween ═══════
    val fastDuration: Int = 150,
    val mediumDuration: Int = 250,
    val slowDuration: Int = 400,
    val extraSlowDuration: Int = 600,
) {
    // ═══════ TWEEN PRESETS (عند الحاجة) ═══════
    val fastTween: FiniteAnimationSpec<Float> get() = tween(fastDuration, easing = FastEasing)
    val mediumTween: FiniteAnimationSpec<Float> get() = tween(mediumDuration, easing = StandardEasing)
    val slowTween: FiniteAnimationSpec<Float> get() = tween(slowDuration, easing = DecelerateEasing)

    // ═══════ STAGGERED ENTRY ═══════
    /** التأخير بين العناصر المتتابعة (ms) */
    val staggerStepMs: Long = 20L

    /** حساب التأخير للعنصر رقم i في قائمة متتابعة */
    fun staggerDelay(index: Int): Long = index * staggerStepMs

    // ═══════ TYPEWRITER ═══════
    /** التأخير لكل حرف في تأثير الكتابة */
    val typewriterCharDelayMs: Long = 30L

    /** التأخير لكل حرف في Streaming (أسرع) */
    val streamingCharDelayMs: Long = 12L

    // ═══════ HAPTIC DELAYS ═══════
    val hapticDebounceMs: Long = 50L
}

// ═══════════════════════════════════════════════════════════════
//  Easing Curves — للاستخدام في tween فقط
// ═══════════════════════════════════════════════════════════════
val FastEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
val StandardEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
val DecelerateEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
val AccelerateEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
val LinearEasingRef: Easing = LinearEasing

// ═══════════════════════════════════════════════════════════════
//  CompositionLocal — للوصول من @Composable
// ═══════════════════════════════════════════════════════════════
val LocalMotion = staticCompositionLocalOf { Motion() }

// ═══════════════════════════════════════════════════════════════
//  AppMotion — object للاستخدام المباشر
// ═══════════════════════════════════════════════════════════════
object AppMotion {
    val motion = Motion()

    // الاختصارات الشائعة
    val fast      get() = motion.fastSpring
    val medium    get() = motion.mediumSpring
    val slow      get() = motion.slowSpring
    val bouncy    get() = motion.bouncySpring
    val soft      get() = motion.softSpring

    val fastOffset   get() = motion.fastSpringOffset
    val mediumOffset get() = motion.mediumSpringOffset
    val slowOffset   get() = motion.slowSpringOffset
    val bouncyOffset get() = motion.bouncySpringOffset

    // Staggered
    fun stagger(index: Int): Long = motion.staggerDelay(index)
}

// ═══════════════════════════════════════════════════════════════
//  Haptic Feedback — Matrix موحّد
// ═══════════════════════════════════════════════════════════════
/**
 * أنواع الـ Haptic حسب الإجراء:
 *
 *   SendMessage     → TextHandleMove    (خفيف جداً)
 *   OpenDialog      → LongPress         (متوسط)
 *   CopyCode        → LongPress         (متوسط)
 *   SaveProject     → Confirm           (قوي، نجاح)
 *   Error           → Reject            (قوي، رفض)
 *   Toggle          → TextHandleMove    (خفيف)
 *   Swipe           → TextHandleMove    (خفيف)
 *   LongPress       → LongPress         (متوسط)
 *
 *  الاستخدام في Composable:
 *    val haptic = LocalHapticFeedback.current
 *    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
 */
object HapticMatrix {
    // ملاحظات فقط — التنفيذ يستخدم HapticFeedbackType مباشرة
    // هذا الملف لا يستورد Compose UI — المصفوفة في ChatMotion.kt / CodeMotion.kt
}

// ═══════════════════════════════════════════════════════════════
//  Spring Presets — Constants (Static access)
// ═══════════════════════════════════════════════════════════════
val SpringFast = spring<Float>(
    dampingRatio = 0.85f,
    stiffness = 800f,
)
val SpringMedium = spring<Float>(
    dampingRatio = 0.85f,
    stiffness = 400f,
)
val SpringSlow = spring<Float>(
    dampingRatio = 0.90f,
    stiffness = 200f,
)
val SpringBouncy = spring<Float>(
    dampingRatio = 0.50f,
    stiffness = 400f,
)
val SpringSoft = spring<Float>(
    dampingRatio = 0.95f,
    stiffness = 300f,
)

// Suppress unused warning for Spring default import
private val _unusedSpring = Spring
