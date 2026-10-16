package com.gptplus18.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════
 *  AppToast — Animated Bottom Toast
 *  UXRD-2026-10-16-001 · Phase 6 · D-6.4
 * ═══════════════════════════════════════════════════════════════
 *
 *  Toast أنيق يظهر أسفل الشاشة مع animation انسيابي.
 *  يستبدل Toast الافتراضي (Android) بتجربة موحّدة واحترافية.
 *
 *  الاستخدام:
 *    var toastMsg by remember { mutableStateOf<String?>(null) }
 *    AppToast(message = toastMsg, type = ToastType.Success)
 *
 *  ⚠️ internal — للاستخدام في كل الشاشات
 */

enum class ToastType { Success, Error, Info }

@Composable
internal fun AppToast(
    message: String?,
    modifier: Modifier = Modifier,
    type: ToastType = ToastType.Success,
) {
    val (bgColor, icon, iconTint) = when (type) {
        ToastType.Success -> Triple(
            Color(0xFF1A2E20),
            Icons.Filled.CheckCircle,
            Color(0xFF5EBF7A),
        )
        ToastType.Error -> Triple(
            Color(0xFF2E1A1A),
            Icons.Outlined.ErrorOutline,
            Color(0xFFE5484D),
        )
        ToastType.Info -> Triple(
            Color(0xFF1A1E2E),
            Icons.Outlined.Info,
            Color(0xFF7BB6F5),
        )
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = 600f,
                ),
            ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = androidx.compose.animation.core.tween(220),
            ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(180)),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = message ?: "",
                    color = Color.White,
                    fontSize = 10.4.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
