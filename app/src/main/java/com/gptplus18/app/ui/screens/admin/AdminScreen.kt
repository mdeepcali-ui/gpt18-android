package com.gptplus18.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.data.models.AdminUser
import com.gptplus18.app.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

enum class AdminTab { USERS, NOTIFICATIONS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(vm: AdminViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var currentTab by remember { mutableStateOf(AdminTab.USERS) }
    var grantEmail by remember { mutableStateOf("") }
    var grantPlan by remember { mutableStateOf("month") }
    var actionEmail by remember { mutableStateOf("") }
    var notifUid by remember { mutableStateOf("") }
    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastBody by remember { mutableStateOf("") }

    LaunchedEffect(state.message, state.error) {
        if (state.message != null || state.error != null) {
            delay(3000)
            vm.clearMessages()
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Accent)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.t_015), color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (state.isOwner) {
                        IconButton(onClick = {
                            vm.loadUsers()
                            vm.loadNotifStats()
                        }) {
                            Icon(Icons.Default.Refresh, stringResource(R.string.t_101), tint = Accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (state.isChecking) {
            Box(Modifier.fillMaxSize().statusBarsPadding().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            return@Scaffold
        }
        if (!state.isOwner) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔒", fontSize = 33.8.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.t_016), color = Error, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.t_017), color = TextSecondary, fontSize = 9.8.sp,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding)) {
            // ⭐ v2.4: Segmented Control احترافي
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgSecondary)
                    .border(1.dp, LocalAppColors.current.surfaceVariant, RoundedCornerShape(14.dp))
                    .padding(4.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AdminTabButton(
                        label = stringResource(R.string.t_018),
                        selected = currentTab == AdminTab.USERS,
                        modifier = Modifier.weight(1f),
                        onClick = { currentTab = AdminTab.USERS },
                    )
                    AdminTabButton(
                        label = stringResource(R.string.t_019),
                        selected = currentTab == AdminTab.NOTIFICATIONS,
                        modifier = Modifier.weight(1f),
                        onClick = { currentTab = AdminTab.NOTIFICATIONS },
                    )
                }
            }
            when (currentTab) {
                AdminTab.USERS -> UsersTab(
                    state = state,
                    vm = vm,
                    grantEmail = grantEmail,
                    onGrantEmailChange = { grantEmail = it },
                    grantPlan = grantPlan,
                    onGrantPlanChange = { grantPlan = it },
                    actionEmail = actionEmail,
                    onActionEmailChange = { actionEmail = it },
                )
                AdminTab.NOTIFICATIONS -> NotificationsTab(
                    state = state,
                    vm = vm,
                    notifUid = notifUid,
                    onNotifUidChange = { notifUid = it },
                    notifTitle = notifTitle,
                    onNotifTitleChange = { notifTitle = it },
                    notifBody = notifBody,
                    onNotifBodyChange = { notifBody = it },
                    broadcastTitle = broadcastTitle,
                    onBroadcastTitleChange = { broadcastTitle = it },
                    broadcastBody = broadcastBody,
                    onBroadcastBodyChange = { broadcastBody = it },
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// USERS TAB
// ═══════════════════════════════════════════
@Composable
private fun UsersTab(
    state: AdminUiState,
    vm: AdminViewModel,
    grantEmail: String,
    onGrantEmailChange: (String) -> Unit,
    grantPlan: String,
    onGrantPlanChange: (String) -> Unit,
    actionEmail: String,
    onActionEmailChange: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            state.message?.let { InfoCard(it, true); Spacer(Modifier.height(8.dp)) }
            state.error?.let { InfoCard(it, false) }
        }
        item {
            Text(stringResource(R.string.t_020), color = Accent, fontSize = 10.6.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    label = stringResource(R.string.t_102),
                    value = state.stats.total,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.People,
                )
                StatCard(
                    label = stringResource(R.string.t_083),
                    value = state.stats.subscribed,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    label = stringResource(R.string.t_103),
                    value = state.stats.trial,
                    color = Color(0xFFFFA726),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                )
                StatCard(
                    label = stringResource(R.string.t_104),
                    value = state.stats.free,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Person,
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CardGiftcard, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.t_021), color = Color(0xFF3B82F6), fontSize = 10.4.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = grantEmail,
                onValueChange = onGrantEmailChange,
                label = { Text(stringResource(R.string.t_022)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanChip("شهري $20", grantPlan == "month", modifier = Modifier.weight(1f)) { onGrantPlanChange("month") }
                PlanChip("سنوي $220", grantPlan == "year", modifier = Modifier.weight(1f)) { onGrantPlanChange("year") }
            }
            Spacer(Modifier.height(8.dp))
            AdminButton(
                label = stringResource(R.string.t_023),
                icon = Icons.Default.CardGiftcard,
                color = Color(0xFF3B82F6),
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.grant(grantEmail, grantPlan); onGrantEmailChange("") },
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.t_024), color = Color(0xFF3B82F6), fontSize = 10.4.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = actionEmail,
                onValueChange = onActionEmailChange,
                label = { Text(stringResource(R.string.t_022)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminButton(
                    label = stringResource(R.string.t_025),
                    icon = Icons.Default.Timer,
                    color = Color(0xFFFFA726),
                    modifier = Modifier.weight(1f),
                    onClick = { vm.resetTrial(actionEmail) },
                )
                AdminButton(
                    label = stringResource(R.string.t_026),
                    icon = Icons.Default.PersonOff,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = { vm.revoke(actionEmail) },
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("المستخدمون (${state.filtered.size})", color = Accent, fontSize = 10.6.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.search,
                onValueChange = { vm.setSearch(it) },
                placeholder = { Text(stringResource(R.string.t_027), color = TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(stringResource(R.string.t_108), state.filter == AdminFilter.ALL) { vm.setFilter(AdminFilter.ALL) }
                FilterChip(stringResource(R.string.t_083), state.filter == AdminFilter.SUBSCRIBED) { vm.setFilter(AdminFilter.SUBSCRIBED) }
                FilterChip(stringResource(R.string.t_103), state.filter == AdminFilter.TRIAL) { vm.setFilter(AdminFilter.TRIAL) }
                FilterChip(stringResource(R.string.t_104), state.filter == AdminFilter.NONE) { vm.setFilter(AdminFilter.NONE) }
            }
        }
        items(state.filtered, key = { it.id }) { u -> UserRow(u) }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

// ═══════════════════════════════════════════
// NOTIFICATIONS TAB
// ═══════════════════════════════════════════
@Composable
private fun NotificationsTab(
    state: AdminUiState,
    vm: AdminViewModel,
    notifUid: String,
    onNotifUidChange: (String) -> Unit,
    notifTitle: String,
    onNotifTitleChange: (String) -> Unit,
    notifBody: String,
    onNotifBodyChange: (String) -> Unit,
    broadcastTitle: String,
    onBroadcastTitleChange: (String) -> Unit,
    broadcastBody: String,
    onBroadcastBodyChange: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            state.message?.let { InfoCard(it, true); Spacer(Modifier.height(8.dp)) }
            state.error?.let { InfoCard(it, false) }
        }
        item {
            Text(stringResource(R.string.t_028), color = Accent, fontSize = 10.6.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    label = stringResource(R.string.t_109),
                    value = state.notifStats.tokensCount,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Notifications,
                )
                StatCard(
                    label = stringResource(R.string.t_110),
                    value = state.notifStats.usersCount,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.People,
                )
                StatCard(
                    label = stringResource(R.string.t_111),
                    value = state.notifStats.sentCount,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Send,
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, null, tint = Warning, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.t_029), color = Warning, fontSize = 10.6.sp, fontWeight = FontWeight.Bold)
            }
            Text(stringResource(R.string.t_030),
                color = TextTertiary, fontSize = 9.sp,
                modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = broadcastTitle,
                onValueChange = onBroadcastTitleChange,
                label = { Text(stringResource(R.string.t_031)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = broadcastBody,
                onValueChange = onBroadcastBodyChange,
                label = { Text(stringResource(R.string.t_032)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            AdminButton(
                label = stringResource(R.string.t_029),
                icon = Icons.Default.Campaign,
                color = Color(0xFF3B82F6),
                modifier = Modifier.fillMaxWidth(),
                loading = state.isSendingNotif,
                onClick = {
                    vm.broadcast(broadcastTitle, broadcastBody)
                    onBroadcastTitleChange(""); onBroadcastBodyChange("")
                },
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Send, null, tint = Accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.t_033), color = Accent, fontSize = 10.6.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notifUid,
                onValueChange = onNotifUidChange,
                label = { Text(stringResource(R.string.t_034)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notifTitle,
                onValueChange = onNotifTitleChange,
                label = { Text(stringResource(R.string.t_031)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notifBody,
                onValueChange = onNotifBodyChange,
                label = { Text(stringResource(R.string.t_032)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            AdminButton(
                label = stringResource(R.string.t_035),
                icon = Icons.Default.Send,
                color = Color(0xFF3B82F6),
                modifier = Modifier.fillMaxWidth(),
                loading = state.isSendingNotif,
                onClick = {
                    val uid = notifUid.trim().toIntOrNull()
                    if (uid != null) {
                        vm.sendNotification(uid, notifTitle, notifBody)
                        onNotifUidChange(""); onNotifTitleChange(""); onNotifBodyChange("")
                    }
                },
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = Accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.t_036), color = Accent, fontSize = 10.6.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (state.notifStats.recent.isEmpty()) {
            item {
                Text(stringResource(R.string.t_037), color = TextTertiary, fontSize = 9.1.sp,
                    modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            items(state.notifStats.recent) { log ->
                NotifLogRow(log.event, log.title ?: "", log.uid, log.sentAt)
            }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun NotifLogRow(event: String, title: String, uid: Int, sentAt: Double) {
    val ago = System.currentTimeMillis() / 1000.0 - sentAt
    val timeText = when {
        ago < 60 -> stringResource(R.string.t_112)
        ago < 3600 -> "قبل ${(ago / 60).toInt()} د"
        ago < 86400 -> "قبل ${(ago / 3600).toInt()} س"
        else -> "قبل ${(ago / 86400).toInt()} ي"
    }

    val emoji = when {
        event.contains("welcome") -> "🎉"
        event.contains("trial") -> "⏳"
        event.contains("sub_expiring") -> "⏰"
        event.contains("sub_expired") -> "❌"
        event.contains("reengage") -> "👋"
        event.contains("update") -> "🚀"
        event.contains("code") -> "💻"
        event.contains("media") -> "🎨"
        event.contains("first_purchase") -> "💎"
        event.contains("new_device") -> "🔒"
        else -> "📬"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 14.1.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title.ifBlank { event },
                    color = TextPrimary, fontSize = 9.1.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1)
                Text("uid: $uid • $timeText",
                    color = TextTertiary, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            // ═══ Icon + Label ═══
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    label,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(8.dp))

            // ═══ Value ═══
            Text(
                "$value",
                color = color,
                fontSize = 19.2.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PlanChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val blue = Color(0xFF3B82F6)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) blue.copy(alpha = 0.12f) else colors.surface)
            .border(
                width = if (active) 1.5.dp else 1.dp,
                color = if (active) blue else colors.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (active) blue else TextSecondary,
            fontSize = 9.6.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (active) Accent else BgTertiary,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            label,
            color = if (active) BgPrimary else TextSecondary,
            fontSize = 9.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun UserRow(u: AdminUser) {
    val now = System.currentTimeMillis() / 1000.0
    val status: String
    val statusColor: Color
    when {
        u.subExpires > now -> { status = stringResource(R.string.t_083); statusColor = Success }
        u.trialExpires > now -> { status = stringResource(R.string.t_103); statusColor = Warning }
        else -> { status = stringResource(R.string.t_104); statusColor = TextTertiary }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(u.name.ifBlank { "?" },
                    color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 10.6.sp)
                if (u.isOwner) {
                    Spacer(Modifier.width(6.dp))
                    Text("👑", fontSize = 9.sp)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(status, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(u.email, color = TextSecondary, fontSize = 9.1.sp)
            if (u.subExpires > now) {
                val days = ((u.subExpires - now) / 86400).toInt()
                Text("متبقي $days يوم",
                    color = TextTertiary, fontSize = 9.sp,
                    modifier = Modifier.padding(top = 4.dp))
            } else if (u.trialExpires > now) {
                val mins = ((u.trialExpires - now) / 60).toInt()
                Text("متبقي $mins دقيقة",
                    color = TextTertiary, fontSize = 9.sp,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(text: String, success: Boolean) {
    val colors = LocalAppColors.current
    val accent = if (success) Color(0xFF10B981) else Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                color = accent,
                fontSize = 9.6.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = Accent,
    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
    cursorColor = Accent,
)

@Composable
private fun AdminTabButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF3B82F6) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color.White else colors.textSecondary,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// ⭐ v2.4: زر موحّد — Outlined أنيق
// ═══════════════════════════════════════════════════════════════
@Composable
private fun AdminButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = !loading) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = color,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
