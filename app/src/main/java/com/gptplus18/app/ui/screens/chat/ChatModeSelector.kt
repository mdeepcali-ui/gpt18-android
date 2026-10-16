package com.gptplus18.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.data.models.ChatMode
import com.gptplus18.app.ui.theme.LocalAppColors

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatModeSelector — Mode Selection Components
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.7
 * ═══════════════════════════════════════════════════════════════
 *
 *  مكوّنات اختيار الوضع:
 *    • ModeDropdown   — عرض الوضع الحالي (Chat)
 *    • ModeMenuItem   — عنصر قائمة منسدلة (احتياطي)
 *
 *  ⚠️ internal — تُستخدم داخل ChatScreen
 */

@Composable
internal fun ModeDropdown(
    current: ChatMode,
    onSelect: (ChatMode) -> Unit,
) {
    // ⭐ v2.0: كل شي في Chat — عنوان فقط بدون قائمة
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Chat", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.1.sp)
    }
}

@Composable
internal fun ModeMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    DropdownMenuItem(
        modifier = Modifier.background(colors.surface),
        colors = MenuDefaults.itemColors(
            textColor = colors.textPrimary,
            leadingIconColor = colors.textPrimary,
        ),
        text = {
            Text(
                label,
                color = if (selected) colors.accent else colors.textPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(icon, null, tint = if (selected) colors.accent else colors.textSecondary)
        },
    )
}
