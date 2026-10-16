package com.gptplus18.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.R
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextTertiary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.runtime.setValue

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatModals — Dialog Components
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.4
 * ═══════════════════════════════════════════════════════════════
 *
 *  الديالوغات الخاصة بالشات:
 *    • SubscriptionRequiredModal — طلب اشتراك
 *    • FeatureBadge              — شارة ميزة
 *    • TopupRequiredModal        — طلب شحن رصيد
 *
 *  ⚠️ جميع الدوال internal — تُستخدم داخل ChatScreen فقط
 */

// ═══════════════════════════════════════════════════════════
// 🔒 Subscription Required Modal — احترافي minimalist
// ═══════════════════════════════════════════════════════════
@Composable
internal fun SubscriptionRequiredModal(
    kind: String,
    message: String,
    onSubscribe: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    if (visible) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(
                        initialScale = 0.92f,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
                        ),
                    ),
                    exit = androidx.compose.animation.fadeOut(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1C1C1E))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            )
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // ⭐ أيقونة premium
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4AF37).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Outlined.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(32.dp),
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "هذا الخيار للمشتركين",
                            color = Color.White,
                            fontSize = 13.4.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "استمتع بجميع الميزات مع إحدى باقاتنا",
                            color = Color(0xFFB8B8C0),
                            fontSize = 9.1.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 19.sp,
                        )

                        Spacer(Modifier.height(24.dp))

                        // ─── صف الأيقونات ───
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            FeatureBadge(androidx.compose.material.icons.Icons.Outlined.Image, "صور")
                            
                            FeatureBadge(androidx.compose.material.icons.Icons.Outlined.MusicNote, "أغاني")
                            FeatureBadge(androidx.compose.material.icons.Icons.Outlined.Code, "كود")
                        }

                        Spacer(Modifier.height(28.dp))

                        // ─── زر أساسي ───
                        val btnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val btnPressed by btnInteractionSource.collectIsPressedAsState()
                        val btnScale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (btnPressed) 0.96f else 1f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessHigh,
                            ),
                            label = "btnScale",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .clickable(
                                    interactionSource = btnInteractionSource,
                                    indication = null,
                                    onClick = { onSubscribe() }
                                )
                                .padding(vertical = 15.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "عرض الباقات",
                                color = Color(0xFF1C1C1E),
                                fontSize = 10.6.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // ─── زر ثانوي نصي ───
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "لاحقاً",
                                color = Color(0xFFB8B8C0),
                                fontSize = 9.8.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FeatureBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF2A2A2E)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFB8B8BC),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = Color(0xFFB8B8C0),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun TopupRequiredModal(
    action: String,
    onTopup: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isArabic = java.util.Locale.getDefault().language == "ar"

    val title = if (isArabic) "تحتاج رصيد" else "Balance Required"

    // ⭐ رسالة حسب الـaction
    val msg: String = if (action == "image_anime") {
        if (isArabic)
            "بدي ارسملك صورة أنمي بس ما فيني هلق 🥺\nضيف رصيد وبعمللك اللي بدك ياه 💜✨"
        else
            "Can't create anime image right now 🥺\nAdd balance and I'll do anything you need 💜✨"
    } else {
        if (isArabic)
            "ما فيني اعمللك هالشي هلق 🥺\nضيف شوي رصيد وبعمللك اللي بدك ياه 🌸"
        else
            "Can't do this right now 🥺\nAdd balance and I'll do anything you need 🌸"
    }

    val btnText = if (isArabic) "اشحن رصيد" else "Top-up Balance"
    val cancelText = if (isArabic) "لاحقاً" else "Later"

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.8.sp,
            )
        },
        text = {
            Text(
                msg,
                color = TextSecondary,
                fontSize = 10.4.sp,
                lineHeight = 22.sp,
            )
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onTopup,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = androidx.compose.ui.graphics.Color(0xFF0F0F0F),
                ),
            ) {
                Text(btnText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(cancelText, color = TextSecondary)
            }
        },
    )
}
