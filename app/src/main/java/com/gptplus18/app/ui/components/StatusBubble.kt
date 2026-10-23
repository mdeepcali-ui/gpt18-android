package com.gptplus18.app.ui.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.LocalAppColors
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.widthIn
import com.gptplus18.app.ui.theme.NotoNaskhFontFamily

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatStatusBubble — Status Indicator
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.2
 * ═══════════════════════════════════════════════════════════════
 *
 *  فقاعة الحالة (يفكر / يحلل / يرفع) — تحت البار العلوي.
 *
 *  الميزات:
 *    • موقع: أعلى اليمين (TopStart في RTL)
 *    • شعاع لامع يمر من اليسار لليمين كل 0.7s
 *    • نقطة زرقاء نابضة
 *
 *  الاستخدام:
 *    StatusBubble(label = "التفكير")
 *    StatusBubble(label = state.statusLabel)
 *
 *  ⚠️ public — يُستخدَم من ChatScreen + CodeScreen
 */

/**
 * ⭐ فقاعة الحالة (يفكر / يحلل / يرفع) — تحت البار العلوي
 * - موقع: أعلى اليمين (TopStart في RTL)
 * - شعاع لامع يمر من اليسار لليمين كل 0.7s
 * - نقطة زرقاء نابضة
 */
@Composable
fun StatusBubble(label: String) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "status")

    // ✨ Shimmer ناعم وبطيء
    val shimmerX by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2200,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "shimmer",
    )

    // 💫 توهج متنفّس — ينبض بنعومة
    val glowAlpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.32f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1800),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "glow",
    )

    // ⭐ دوران أنيق للنجمة
    val starRot by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 8000,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "star",
    )

    // 🎨 Palette theme-aware
    val _isLight = LocalAppColors.current.isLight
    val bgTop     = if (_isLight) Color(0xFFFFFFFF) else Color(0xFF17181C)
    val bgBot     = if (_isLight) Color(0xFFF4F5F8) else Color(0xFF0B0C0F)
    val glowColor = if (_isLight) Color(0xFF8FA8FF) else Color(0xFF6D8FFF)
    val textBase  = if (_isLight) Color(0xFF4A4F5C) else Color(0xFFB8BDC9)
    val textShine = if (_isLight) Color(0xFF1A1A1C) else Color(0xFFFFFFFF)
    val starTint  = if (_isLight) Color(0xFF5A73D9) else Color(0xFFB8C5FF)

    Box(
        modifier = Modifier
            // ✨ توهج خارجي ناعم (بدل الإطار الصلب)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = glowColor.copy(alpha = glowAlpha),
                spotColor = glowColor.copy(alpha = glowAlpha),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(bgTop, bgBot)))
            .border(1.6.dp, Brush.linearGradient(listOf(Color(0xFF3D2F22), Color(0xFF5A4938), Color(0xFF3D2F22))), RoundedCornerShape(20.dp))
    ) {
        // 💎 لمعة داخلية خفيفة بالأعلى
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            (if (_isLight) Color.Black else Color.White).copy(alpha = 0.045f),
                            Color.Transparent,
                        )
                    )
                )
        )

        // ✨ مسح لامع يمر بنعومة
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF8FA8FF).copy(alpha = 0.09f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                            Color.Transparent,
                        ),
                        startX = shimmerX * 420f,
                        endX   = shimmerX * 420f + 230f,
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .widthIn(min = 118.dp)
                .padding(horizontal = 16.dp, vertical = 9.dp),
        ) {
            // ⭐ نجمة تدور بأناة
            androidx.compose.material3.Text(
                text = "\u2726",
                color = starTint.copy(alpha = 0.92f),
                fontSize = 9.6.sp,
                modifier = Modifier.graphicsLayer(rotationZ = starRot),
            )

            // ✍️ نص بمقاس راقي + مسح لامع
            androidx.compose.foundation.layout.Box {
                listOf(-0.3f, 0f, 0.3f).forEach { dx ->
                    androidx.compose.foundation.text.BasicText(
                        text = label,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = NotoNaskhFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    textBase, textBase,
                                    textShine, textShine, textShine,
                                    textBase, textBase,
                                ),
                                startX = shimmerX * 600f,
                                endX   = shimmerX * 600f + 160f,
                            ),
                        ),
                        modifier = androidx.compose.ui.Modifier.offset(x = dx.dp),
                    )
                }
            }
        }
    }
}
