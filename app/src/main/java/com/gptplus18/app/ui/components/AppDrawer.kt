package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.*

@Composable
fun AppDrawerContent(
    userName: String,
    userEmail: String,
    isOwner: Boolean,
    onNewChat: () -> Unit,
    onChat: () -> Unit,
    onCode: () -> Unit,
    onMedia: () -> Unit,
    onSubscription: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit = {},
    onAdmin: () -> Unit,
    onLanguageToggle: () -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = BgSecondary,
        modifier = Modifier.width(300.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        // ═══ Header: صورة + اسم + إيميل ═══
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(Accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    userName.take(1).uppercase().ifEmpty { "?" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(userName.ifEmpty { "مستخدم" }, color = TextPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(userEmail, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Divider(color = TextSecondary.copy(alpha = 0.15f))
        Spacer(Modifier.height(8.dp))

        // ═══ محادثة جديدة ═══
        DrawerItem("محادثة جديدة", Icons.Default.Add, onNewChat, Accent)

        Spacer(Modifier.height(4.dp))
        Divider(color = TextSecondary.copy(alpha = 0.15f))
        Spacer(Modifier.height(4.dp))

        // ═══ الأقسام الأساسية ═══
        DrawerItem("الاشتراك", Icons.Default.CreditCard, onSubscription)
        DrawerItem("الملف الشخصي", Icons.Default.Person, onProfile)
        DrawerItem("الإعدادات", Icons.Default.Settings, onSettings)

        if (isOwner) {
            Spacer(Modifier.height(4.dp))
            Divider(color = TextSecondary.copy(alpha = 0.15f))
            Spacer(Modifier.height(4.dp))
            DrawerItem("لوحة المالك", Icons.Default.AdminPanelSettings, onAdmin, Accent)
        }

        Spacer(Modifier.weight(1f))

        // ═══ الأسفل ═══
        Divider(color = TextSecondary.copy(alpha = 0.15f))
        DrawerItem("اللغة (AR/EN)", Icons.Default.Language, onLanguageToggle)
        DrawerItem("تسجيل خروج", Icons.AutoMirrored.Filled.Logout, onLogout, Color(0xFFEF4444))

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = TextPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
