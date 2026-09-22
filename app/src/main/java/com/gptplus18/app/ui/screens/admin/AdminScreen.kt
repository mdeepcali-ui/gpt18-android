package com.gptplus18.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(vm: AdminViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    var grantEmail by remember { mutableStateOf("") }
    var grantPlan by remember { mutableStateOf("month") }
    var actionEmail by remember { mutableStateOf("") }

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
                        IconButton(onClick = { vm.loadUsers() }) {
                            Icon(Icons.Default.Refresh, "تحديث", tint = Accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        // ═══ حماية المالك ═══
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

        // ═══ للمالك ═══
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ─── رسائل ───
            item {
                state.message?.let {
                    InfoCard(it, true)
                    Spacer(Modifier.height(8.dp))
                }
                state.error?.let {
                    InfoCard(it, false)
                }
            }

            // ─── الإحصائيات ───
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

            // ─── منح اشتراك ───
            item {
                Spacer(Modifier.height(8.dp))
                Text("منح اشتراك", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = grantEmail,
                    onValueChange = { grantEmail = it },
                    label = { Text("بريد المستخدم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = adminFieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanChip("شهري $20", grantPlan == "month") { grantPlan = "month" }
                    PlanChip("سنوي $220", grantPlan == "year") { grantPlan = "year" }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        vm.grant(grantEmail, grantPlan)
                        grantEmail = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("منح", color = BgPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // ─── إجراءات على مستخدم ───
            item {
                Spacer(Modifier.height(8.dp))
                Text("إجراءات على مستخدم", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = actionEmail,
                    onValueChange = { actionEmail = it },
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
                    ) {
                        Text("إعادة تجربة", color = BgPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { vm.revoke(actionEmail) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("إلغاء", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // ─── بحث + فلتر ───
            item {
                Spacer(Modifier.height(8.dp))
                Text("المستخدمون (${state.filtered.size})",
                    color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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

            // ─── قائمة المستخدمين ───
            items(state.filtered, key = { it.id }) { u ->
                UserRow(u)
            }

            item { Spacer(Modifier.height(60.dp)) }
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
