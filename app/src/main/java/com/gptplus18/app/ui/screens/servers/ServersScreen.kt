package com.gptplus18.app.ui.screens.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.ui.theme.LocalAppColors
import androidx.compose.material.icons.outlined.Cloud
import com.gptplus18.app.ui.theme.TajawalFontFamily

private val AccentBlue = Color(0xFF3B82F6)
private val Danger = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    onBack: () -> Unit,
    vm: ServersViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    val bgPrimary = colors.bg
    val bgSecondary = colors.surface
    val textPrimary = colors.textPrimary
    val textSecondary = colors.textSecondary

    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.toast) {
        if (state.toast.isNotBlank()) {
            snackbarHost.showSnackbar(state.toast)
            vm.clearToast()
        }
    }
    LaunchedEffect(state.error) {
        if (state.error.isNotBlank()) {
            snackbarHost.showSnackbar("⚠️ ${state.error}")
            vm.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("🌐 سيرفراتي", color = textPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع", tint = textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.showAdd() }) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(2.dp, AccentBlue, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Add, "إضافة", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgSecondary),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = bgPrimary,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bgPrimary),
        ) {
            when {
                state.loading && state.servers.isEmpty() -> {
                    com.gptplus18.app.ui.components.SkeletonList(count = 4)
                }
                state.servers.isEmpty() -> {
                    EmptyServers(textPrimary, textSecondary) { vm.showAdd() }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.servers, key = { it.id }) { s ->
                            ServerCard(
                                server = s,
                                testing = state.testing == s.id,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                bgSecondary = bgSecondary,
                                onTest = { vm.test(s.id) },
                                onDelete = { vm.delete(s.id) },
                            )
                        }
                    }
                }
            }

            if (state.showAddDialog) {
                AddServerDialog(
                    onDismiss = { vm.hideAdd() },
                    onSubmit = { label, host, port, user, authType, secret ->
                        vm.add(label, host, port, user, authType, secret)
                    },
                )
            }
        }
    }
}

@Composable
private fun EmptyServers(textPrimary: Color, textSecondary: Color, onAdd: () -> Unit) {
    com.gptplus18.app.ui.components.EmptyState(
        icon = androidx.compose.material.icons.Icons.Outlined.Cloud,
        title = "لا يوجد سيرفرات مربوطة",
        description = "أضف سيرفر SSH حتى يتمكن مساعدك الذكي من نشر مشاريعك وتشغيلها مباشرة",
        ctaText = "+ إضافة سيرفر",
        onCtaClick = onAdd,
    )
}

@Composable
private fun ServerCard(
    server: com.gptplus18.app.data.models.ServerItem,
    testing: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    bgSecondary: Color,
    onTest: () -> Unit,
    onDelete: () -> Unit,
) {
    val statusIcon = when (server.lastStatus) {
        "ok" -> "✅"
        "fail" -> "❌"
        else -> "⚪"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgSecondary)
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🖥️", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(server.label, color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 11.2.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${server.username}@${server.host}:${server.port}",
                        color = textSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Text(statusIcon, fontSize = 12.8.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, AccentBlue, RoundedCornerShape(8.dp))
                        .clickable(enabled = !testing) { onTest() }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    if (testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = AccentBlue,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("اختبار", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Danger.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onDelete() }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, null, tint = Danger, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("حذف", color = Danger, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddServerDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Int, String, String, String) -> Unit,
) {
    val bgBlack = Color(0xFF0F0F0F)
    val bgMid = Color(0xFF1A1A1A)
    val textPrimary = Color(0xFFEDEDED)
    val textDim = Color(0xFF8A8A8A)

    var label by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var user by remember { mutableStateOf("") }
    var authType by remember { mutableStateOf("password") }
    var secret by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bgBlack)
                .border(1.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("🌐 سيرفر جديد", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                DialogField("الاسم (مثل production)", label, bgMid, textPrimary, textDim) { label = it }
                DialogField("Host (IP أو دومين)", host, bgMid, textPrimary, textDim) { host = it }
                DialogField("Port", port, bgMid, textPrimary, textDim) { port = it }
                DialogField("المستخدم (root/deploy)", user, bgMid, textPrimary, textDim) { user = it }

                // Auth type toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuthChip("كلمة السر", authType == "password") { authType = "password" }
                    AuthChip("مفتاح SSH", authType == "key") { authType = "key" }
                }

                DialogField(
                    if (authType == "password") "كلمة السر" else "private key",
                    secret, bgMid, textPrimary, textDim,
                    isPassword = authType == "password",
                ) { secret = it }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f).height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, textDim.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) { Text("إلغاء", color = textDim, fontSize = 10.4.sp) }

                    Box(
                        modifier = Modifier
                            .weight(2f).height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentBlue)
                            .clickable {
                                onSubmit(label, host, port.toIntOrNull() ?: 22, user, authType, secret)
                            },
                        contentAlignment = Alignment.Center,
                    ) { Text("حفظ", color = Color.White, fontSize = 10.4.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun DialogField(
    placeholder: String,
    value: String,
    bg: Color,
    textColor: Color,
    dimColor: Color,
    isPassword: Boolean = false,
    onChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(placeholder, color = dimColor, fontSize = 9.6.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(fontFamily = TajawalFontFamily, color = textColor, fontSize = 10.4.sp),
            cursorBrush = SolidColor(AccentBlue),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AuthChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) AccentBlue.copy(alpha = 0.15f) else Color(0xFF0F0F0F))
            .border(1.dp, if (selected) AccentBlue else Color(0xFF8A8A8A).copy(alpha = 0.3f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) AccentBlue else Color(0xFF8A8A8A),
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
