package com.gptplus18.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ═══════════════════════════════════════════════════════════════
 *  SkeletonLoader — Shimmer Placeholders
 *  UXRD-2026-10-16-001 · Phase 6 · D-6.2
 * ═══════════════════════════════════════════════════════════════
 *
 *  هياكل تحميل بدل الـ spinners:
 *    • SkeletonBox     — مستطيل واحد
 *    • SkeletonBubble  — فقاعة شات
 *    • SkeletonCard    — بطاقة قائمة
 *    • SkeletonList    — قائمة كاملة
 *
 *  ⚠️ internal — للاستخدام في كل الشاشات
 */

@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val x by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton_x",
    )
    val base = Color(0xFF2A2A2E)
    val shine = Color(0xFF3D3D42)
    return Brush.linearGradient(
        colors = listOf(base, shine, base),
        start = Offset(x * 500f - 200f, 0f),
        end = Offset(x * 500f + 200f, 0f),
    )
}

@Composable
internal fun SkeletonBox(
    width: Dp? = null,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush()),
    )
}

@Composable
internal fun SkeletonBubble(
    isUser: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) {
            androidx.compose.foundation.layout.Arrangement.End
        } else {
            androidx.compose.foundation.layout.Arrangement.Start
        },
    ) {
        Column(
            modifier = Modifier
                .width(if (isUser) 200.dp else 280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isUser) Color(0xFF2A2A2E) else Color(0xFF1A1A1D)
                )
                .padding(12.dp),
        ) {
            SkeletonBox(height = 12.dp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            SkeletonBox(
                height = 12.dp,
                modifier = Modifier.fillMaxWidth(if (isUser) 0.6f else 0.85f),
            )
            if (!isUser) {
                Spacer(Modifier.height(6.dp))
                SkeletonBox(height = 12.dp, modifier = Modifier.fillMaxWidth(0.5f))
            }
        }
    }
}

@Composable
internal fun SkeletonCard(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1D))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // أيقونة دائرية
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(shimmerBrush()),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBox(height = 13.dp, modifier = Modifier.fillMaxWidth(0.7f))
            Spacer(Modifier.height(6.dp))
            SkeletonBox(height = 11.dp, modifier = Modifier.fillMaxWidth(0.4f))
        }
    }
}

@Composable
internal fun SkeletonList(
    count: Int = 3,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        repeat(count) {
            SkeletonCard()
            if (it < count - 1) Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
internal fun SkeletonChatList(
    count: Int = 4,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(count) { i ->
            SkeletonBubble(isUser = i % 2 == 0)
        }
    }
}
