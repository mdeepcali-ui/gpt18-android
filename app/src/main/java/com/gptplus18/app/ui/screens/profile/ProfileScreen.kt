package com.gptplus18.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: ProfileViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = { Text("الملف الشخصي", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(100.dp).background(Accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    state.userName.take(1).uppercase().ifEmpty { "?" },
                    fontSize = 42.sp, fontWeight = FontWeight.Bold, color = BgPrimary,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(state.userName.ifEmpty { "مستخدم" }, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(state.userEmail, color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("حالة الاشتراك", color = Accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            state.hasSubscription -> "مشترك"
                            state.hasTrial -> "تجربة مجانية"
                            else -> "بدون اشتراك"
                        },
                        color = TextPrimary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.DarkMode, null, tint = Accent)
                Spacer(Modifier.width(12.dp))
                Text("الوضع الليلي", color = TextPrimary, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.darkMode,
                    onCheckedChange = { vm.toggleDarkMode(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Language, null, tint = Accent)
                Spacer(Modifier.width(12.dp))
                Text("اللغة", color = TextPrimary, modifier = Modifier.weight(1f))
                TextButton(onClick = { vm.toggleLanguage() }) {
                    Text(state.language.uppercase(), color = Accent)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { vm.logout() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            ) {
                Icon(Icons.Default.Logout, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("تسجيل خروج", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
