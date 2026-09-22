package com.gptplus18.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * يوفر Font Scale ديناميكي لكل المحتوى
 *
 * يستخدم LocalDensity لتغيير الـ fontScale — بدون الحاجة لتغيير
 * كل TextStyle بشكل منفصل.
 *
 * @param fontScale قيمة من 0.75 إلى 1.5 (افتراضي 1.0)
 * @param content المحتوى اللي رح يتأثر
 */
@Composable
fun FontScaleProvider(
    fontScale: Float,
    content: @Composable () -> Unit,
) {
    val current = LocalDensity.current
    val newDensity = Density(
        density = current.density,
        fontScale = current.fontScale * fontScale,
    )
    CompositionLocalProvider(
        LocalDensity provides newDensity,
        content = content,
    )
}
