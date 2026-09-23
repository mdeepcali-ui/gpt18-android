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
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@Composable
fun AppDrawerContent(
    userName: String,
    userEmail: String,
    isOwner: Boolean,
    onNewChat: () -> Unit,
    onSessionsClick: () -> Unit = {},
    lastSessionTitle: String? = null,
    onChat: () -> Unit,
    onCode: () -> Unit,
    onMedia: () -> Unit,
    onSubscription: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit = {},
    onAdmin: () -> Unit,
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
                Text(userName.ifEmpty { stringResource(R.string.t_196) }, color = TextPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(userEmail, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Divider(color = TextSecondary.copy(alpha = 0.15f))
        Spacer(Modifier.height(8.dp))

        // ═══ محادثة جديدة ═══
        DrawerItem(stringResource(R.string.new_chat), Icons.Default.Add, onNewChat, Accent)

        // ═══ سجل محادثات ═══
        DrawerItem(
            label = stringResource(R.string.t_226),
            icon = Icons.Default.History,
            onClick = onSessionsClick,
            tint = TextPrimary,
            subtitle = lastSessionTitle,
        )

        Spacer(Modifier.height(4.dp))
        Divider(color = TextSecondary.copy(alpha = 0.15f))
        Spacer(Modifier.height(4.dp))

        // ═══ الأقسام الأساسية ═══
        DrawerItem(stringResource(R.string.t_079), Icons.Default.CreditCard, onSubscription)
        DrawerItem(stringResource(R.string.t_069), Icons.Default.Person, onProfile)
        DrawerItem(stringResource(R.string.t_076), Icons.Default.Settings, onSettings)

        if (isOwner) {
            Spacer(Modifier.height(4.dp))
            Divider(color = TextSecondary.copy(alpha = 0.15f))
            Spacer(Modifier.height(4.dp))
            DrawerItem(stringResource(R.string.t_071), Icons.Default.AdminPanelSettings, onAdmin, Accent)
        }

        Spacer(Modifier.weight(1f))

        // ═══ الأسفل ═══
        Divider(color = TextSecondary.copy(alpha = 0.15f))
        DrawerItem(stringResource(R.string.t_077), Icons.AutoMirrored.Filled.Logout, onLogout, Color(0xFFEF4444))

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = TextPrimary,
    subtitle: String? = null,
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
        Column(Modifier.weight(1f)) {
            Text(label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            subtitle?.let {
                Text(
                    it.take(30),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                )
            }
        }
    }
}
