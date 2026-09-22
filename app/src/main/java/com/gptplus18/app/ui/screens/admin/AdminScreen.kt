package com.gptplus18.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
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
                        Text("إعدادات المشتركين", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (state.isOwner) {
                        IconButton(onClick = {
                            vm.loadUsers()
                            vm.loadNotifStats()
                        }) {
                            Icon(Icons.Default.Refresh, "تحديث", tint = Accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        if (state.isChecking) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            return@Scaffold
        }
        if (!state.isOwner) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔒", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("غير مصرح", color = Error, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("هذه الصفحة للمالك فقط", color = TextSecondary, fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = BgSecondary,
                contentColor = Accent,
            ) {
                Tab(
                    selected = currentTab == AdminTab.USERS,
                    onClick = { currentTab = AdminTab.USERS },
                    text = { Text("👥 المستخدمون") },
                )
                Tab(
                    selected = currentTab == AdminTab.NOTIFICATIONS,
                    onClick = { currentTab = AdminTab.NOTIFICATIONS },
                    text = { Text("🔔 الإشعارات") },
                )
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
            Text("الإحصائيات", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("كلياً", state.stats.total, Color(0xFF93E0FF), Modifier.weight(1f))
                StatCard("مشترك", state.stats.subscribed, Success, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("تجربة", state.stats.trial, Warning, Modifier.weight(1f))
                StatCard("مجاني", state.stats.free, TextSecondary, Modifier.weight(1f))
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("منح اشتراك", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = grantEmail,
                onValueChange = onGrantEmailChange,
                label = { Text("بريد المستخدم") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanChip("شهري $20", grantPlan == "month") { onGrantPlanChange("month") }
                PlanChip("سنوي $220", grantPlan == "year") { onGrantPlanChange("year") }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.grant(grantEmail, grantPlan); onGrantEmailChange("") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(10.dp),
            ) { Text("منح", color = BgPrimary, fontWeight = FontWeight.Bold) }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("إجراءات على مستخدم", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = actionEmail,
                onValueChange = onActionEmailChange,
                label = { Text("بريد المستخدم") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.resetTrial(actionEmail) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Warning),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("إعادة تجربة", color = BgPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                Button(
                    onClick = { vm.revoke(actionEmail) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("إلغاء", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("المستخدمون (${state.filtered.size})", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.search,
                onValueChange = { vm.setSearch(it) },
                placeholder = { Text("ابحث بالإيميل أو الاسم...", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip("الكل", state.filter == AdminFilter.ALL) { vm.setFilter(AdminFilter.ALL) }
                FilterChip("مشترك", state.filter == AdminFilter.SUBSCRIBED) { vm.setFilter(AdminFilter.SUBSCRIBED) }
                FilterChip("تجربة", state.filter == AdminFilter.TRIAL) { vm.setFilter(AdminFilter.TRIAL) }
                FilterChip("مجاني", state.filter == AdminFilter.NONE) { vm.setFilter(AdminFilter.NONE) }
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
            Text("📊 إحصائيات الإشعارات", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("أجهزة", state.notifStats.tokensCount, Color(0xFF93E0FF), Modifier.weight(1f))
                StatCard("مستخدمون", state.notifStats.usersCount, Success, Modifier.weight(1f))
                StatCard("مُرسل", state.notifStats.sentCount, Accent, Modifier.weight(1f))
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, null, tint = Warning, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("بث للكل", color = Warning, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text("⚠️ يرسل لكل المستخدمين — استخدمه بحكمة",
                color = TextTertiary, fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = broadcastTitle,
                onValueChange = onBroadcastTitleChange,
                label = { Text("العنوان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = broadcastBody,
                onValueChange = onBroadcastBodyChange,
                label = { Text("النص") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    vm.broadcast(broadcastTitle, broadcastBody)
                    onBroadcastTitleChange(""); onBroadcastBodyChange("")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Warning),
                shape = RoundedCornerShape(10.dp),
                enabled = !state.isSendingNotif,
            ) {
                if (state.isSendingNotif) {
                    CircularProgressIndicator(color = BgPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Campaign, null, tint = BgPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("بث للكل", color = BgPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Send, null, tint = Accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("إرسال لمستخدم", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notifUid,
                onValueChange = onNotifUidChange,
                label = { Text("UID المستخدم (رقم)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notifTitle,
                onValueChange = onNotifTitleChange,
                label = { Text("العنوان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notifBody,
                onValueChange = onNotifBodyChange,
                label = { Text("النص") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                colors = adminFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val uid = notifUid.trim().toIntOrNull()
                    if (uid != null) {
                        vm.sendNotification(uid, notifTitle, notifBody)
                        onNotifUidChange(""); onNotifTitleChange(""); onNotifBodyChange("")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(10.dp),
                enabled = !state.isSendingNotif,
            ) {
                Icon(Icons.Default.Send, null, tint = BgPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("إرسال", color = BgPrimary, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = Accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("آخر الإشعارات المُرسلة", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (state.notifStats.recent.isEmpty()) {
            item {
                Text("ما في إشعارات مُرسلة بعد", color = TextTertiary, fontSize = 13.sp,
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
        ago < 60 -> "قبل قليل"
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
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title.ifBlank { event },
                    color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1)
                Text("uid: $uid • $timeText",
                    color = TextTertiary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("$value", color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondary, fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun PlanChip(label: String, active: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (active) Accent else BgTertiary,
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.weight(1f),
    ) {
        Text(
            label,
            color = if (active) BgPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
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
            fontSize = 12.sp,
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
        u.subExpires > now -> { status = "مشترك"; statusColor = Success }
        u.trialExpires > now -> { status = "تجربة"; statusColor = Warning }
        else -> { status = "مجاني"; statusColor = TextTertiary }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(u.name.ifBlank { "?" },
                    color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (u.isOwner) {
                    Spacer(Modifier.width(6.dp))
                    Text("👑", fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(u.email, color = TextSecondary, fontSize = 13.sp)
            if (u.subExpires > now) {
                val days = ((u.subExpires - now) / 86400).toInt()
                Text("متبقي $days يوم",
                    color = TextTertiary, fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp))
            } else if (u.trialExpires > now) {
                val mins = ((u.trialExpires - now) / 60).toInt()
                Text("متبقي $mins دقيقة",
                    color = TextTertiary, fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(text: String, success: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (success) Success.copy(alpha = 0.15f) else Error.copy(alpha = 0.15f),
        ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(text,
            color = if (success) Success else Error,
            fontSize = 13.sp,
            modifier = Modifier.padding(12.dp))
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
