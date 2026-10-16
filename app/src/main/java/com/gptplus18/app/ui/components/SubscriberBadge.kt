package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════
// ⭐ v2.5: نظام الشارات المميزة للمشتركين
// ═══════════════════════════════════════════════════════════════

// ═══ الألوان الموحّدة للباقات ═══
object PlanColors {
    val Chat = Color(0xFF3B82F6)   // أزرق
    val Code = Color(0xFF10B981)   // أخضر
    val Full = Color(0xFFFFB800)   // ذهبي
    val Free = Color(0xFF6B7280)   // رمادي

    fun forPlan(plan: String?): Color = when (plan) {
        "chat" -> Chat
        "code" -> Code
        "full" -> Full
        else -> Free
    }
}

/**
 * شارة المشترك — تظهر بجانب الاسم
 * - 💬 للدردشة
 * - 💻 للبرمجة
 * - 👑 للكامل
 */
@Composable
fun SubscriberBadge(
    plan: String?,
    modifier: Modifier = Modifier,
) {
    if (plan == null) return  // لا شارة للمجاني

    val color = PlanColors.forPlan(plan)
    val (label, icon) = when (plan) {
        "chat" -> "Pro" to Icons.Default.Forum
        "code" -> "Dev" to Icons.Default.Code
        "full" -> "VIP" to Icons.Default.WorkspacePremium
        else -> return
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
