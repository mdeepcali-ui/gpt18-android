package com.gptplus18.app.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gptplus18.app.R
import com.gptplus18.app.ui.theme.*

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
    avatarUrl: String = "",
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0A0A0C),
        modifier = Modifier.width(310.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            DrawerHeader(userName, userEmail, avatarUrl)

            Spacer(Modifier.height(6.dp))

            // ═══ القسم الرئيسي ═══
            DrawerSectionLabel("\u0627\u0644\u0631\u0626\u064a\u0633\u064a\u0629", "\u26a1")

            DrawerItemNew(
                icon = Icons.Default.Add,
                label = stringResource(R.string.new_chat),
                subtitle = "\u0627\u0628\u062f\u0623 \u0645\u062d\u0627\u062f\u062b\u0629 \u062c\u062f\u064a\u062f\u0629",
                iconTint = Accent,
                onClick = onNewChat,
                highlight = true,
            )

            DrawerItemNew(
                icon = Icons.Default.History,
                label = stringResource(R.string.t_226),
                subtitle = lastSessionTitle?.take(35),
                iconTint = Color(0xFF4ADE80),
                onClick = onSessionsClick,
            )

            Spacer(Modifier.height(4.dp))

            // ═══ الأقسام ═══
            DrawerSectionLabel("\u0627\u0644\u0623\u0642\u0633\u0627\u0645", "\ud83c\udfaf")

            DrawerItemNew(
                icon = Icons.Default.CreditCard,
                label = stringResource(R.string.t_079),
                subtitle = "\u0627\u0644\u062e\u0637\u0629 \u0648\u0627\u0644\u0627\u0634\u062a\u0631\u0627\u0643",
                iconTint = Color(0xFFFFB800),
                onClick = onSubscription,
            )

            DrawerItemNew(
                icon = Icons.Default.Person,
                label = stringResource(R.string.t_069),
                subtitle = "\u0627\u0644\u062d\u0633\u0627\u0628 \u0648\u0627\u0644\u0635\u0648\u0631\u0629",
                iconTint = Color(0xFFA855F7),
                onClick = onProfile,
            )

            DrawerItemNew(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.t_076),
                subtitle = "\u0627\u0644\u0625\u0639\u062f\u0627\u062f\u0627\u062a \u0627\u0644\u0639\u0627\u0645\u0629",
                iconTint = Color(0xFF6B7280),
                onClick = onSettings,
            )

            if (isOwner) {
                Spacer(Modifier.height(4.dp))
                DrawerSectionLabel("\u0627\u0644\u0625\u062f\u0627\u0631\u0629", "\ud83d\udee1\ufe0f")

                DrawerItemNew(
                    icon = Icons.Default.AdminPanelSettings,
                    label = stringResource(R.string.t_071),
                    subtitle = "\u0644\u0648\u062d\u0629 \u0627\u0644\u0645\u0627\u0644\u0643",
                    iconTint = Color(0xFFFF7A7A),
                    onClick = onAdmin,
                )
            }

            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1F1F26)))
            Spacer(Modifier.height(8.dp))

            // ═══ تسجيل خروج ═══
            DrawerItemNew(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = stringResource(R.string.t_077),
                subtitle = null,
                iconTint = Color(0xFFEF4444),
                onClick = onLogout,
            )

            Spacer(Modifier.height(24.dp))

            // ═══ Footer ═══
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "GPT+18 \u00b7 v1.1.0",
                    color = Color(0xFF3F3F46),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DrawerHeader(userName: String, userEmail: String, avatarUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14141A),
                        Color(0xFF0A0A0C),
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column {
            // Avatar + status
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A22))
                        .border(2.dp, Accent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val avatarModel = remember(avatarUrl) {
                        if (avatarUrl.startsWith("data:image")) {
                            try {
                                val b = Base64.decode(avatarUrl.substringAfter(","), Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(b, 0, b.size)
                            } catch (_: Exception) { null }
                        } else if (avatarUrl.isNotBlank()) avatarUrl
                        else null
                    }
                    if (avatarModel != null) {
                        AsyncImage(
                            model = avatarModel,
                            contentDescription = "avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        Text(
                            userName.take(1).uppercase().ifEmpty { "?" },
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent,
                        )
                    }
                }
                // نقطة خضراء (online)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A0A0C))
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80)),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                userName.ifEmpty { stringResource(R.string.t_196) },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                userEmail,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String, emoji: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            color = Color(0xFF52525B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DrawerItemNew(
    icon: ImageVector,
    label: String,
    subtitle: String?,
    iconTint: Color,
    onClick: () -> Unit,
    highlight: Boolean = false,
) {
    var pressed by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        targetValue = when {
            pressed -> Color(0xFF1F1F26)
            highlight -> Accent.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "drawerItemBg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable {
                pressed = true
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // أيقونة دائرية
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f))
                .border(1.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                color = if (highlight) Accent else TextPrimary,
                fontSize = 14.sp,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            )
            if (subtitle != null && subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    color = Color(0xFF52525B),
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text(
            "\u203a",
            color = Color(0xFF3F3F46),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
