package com.gptplus18.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary

/**
 * ⭐ v5.0.8: زر "انزل لآخر المحادثة"
 *
 * مميزات:
 *  - Animation ناعم (scale + fade) عند الظهور/الاختفاء
 *  - Gradient جميل (Accent → لون أعمق)
 *  - تأثير pressing (تصغير بسيط)
 *  - موحّد بين الشات والبرمجة
 */
@Composable
fun ScrollToBottomFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
) {
    val colors = LocalAppColors.current
    val isLight = colors.isLight

    // حلقة أنيقة (glow خفيف)
    val ringColor = if (isLight) Color(0x33000000) else Color(0x33FFFFFF)
    val btnBg = BgSecondary

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(
            initialScale = 0.6f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.6f),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            btnBg,
                            if (isLight) Color(0xFFE8E8EE) else Color(0xFF14161C),
                        ),
                    ),
                )
                .border(1.5.dp, Accent.copy(alpha = 0.65f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // هالة خفيفة حول الأيقونة
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.10f)),
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "انزل لآخر المحادثة",
                tint = Accent,
                modifier = Modifier.size(26.dp),
            )

            // شارة عدد الرسائل الجديدة (اختياري — يظهر لما > 0)
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
        }
    }
}
