package com.gptplus18.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onAbout: () -> Unit = {},
    vm: SettingsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // ─── المظهر ───
            SectionTitle("المظهر")

            SettingRow(
                icon = Icons.Default.DarkMode,
                title = "الوضع الليلي",
                subtitle = if (state.darkMode) "مفعّل" else "معطّل",
            ) {
                Switch(
                    checked = state.darkMode,
                    onCheckedChange = { vm.toggleDarkMode(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent),
                )
            }

            // حجم الخط
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSecondary, RoundedCornerShape(12.dp))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatSize, null, tint = Accent, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("حجم النص", color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text(fontScaleLabel(state.fontScale), color = Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FontSizeChip("صغير", state.fontScale == 0.85f) { vm.setFontScale(0.85f) }
                    FontSizeChip("عادي", state.fontScale == 1.0f) { vm.setFontScale(1.0f) }
                    FontSizeChip("كبير", state.fontScale == 1.15f) { vm.setFontScale(1.15f) }
                    FontSizeChip("ضخم", state.fontScale == 1.3f) { vm.setFontScale(1.3f) }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ─── اللغة ───
            SectionTitle("اللغة")

            SettingRow(
                icon = Icons.Default.Language,
                title = "لغة التطبيق",
                subtitle = if (state.language == "ar") "العربية" else "English",
            ) {
                TextButton(onClick = { vm.toggleLanguage() }) {
                    Text(if (state.language == "ar") "العربية" else "English", color = Accent, fontSize = 13.sp)
                }
            }

            // ─── الإشعارات ───
            SectionTitle("الإشعارات")

            SettingRow(
                icon = Icons.Default.Notifications,
                title = "إشعارات Push",
                subtitle = "قريباً",
            ) {
                Switch(
                    checked = false,
                    onCheckedChange = { },
                    enabled = false,
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent),
                )
            }

            // ─── أخرى ───
            SectionTitle("أخرى")

            SettingRow(
                icon = Icons.Default.Refresh,
                title = "فحص التحديثات",
                subtitle = "الإصدار الحالي: 1.0.0",
                onClick = { vm.checkUpdates() },
            )

            SettingRow(
                icon = Icons.Default.Info,
                title = "عن التطبيق",
                subtitle = "GPT+18 — ذكاء بلا قيود",
                onClick = onAbout,
            )

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp),
    )
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSecondary, RoundedCornerShape(12.dp))
            .then(
                if (onClick != null)
                    Modifier.padding(14.dp)
                else Modifier.padding(14.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp)
            subtitle?.let {
                Text(it, color = TextSecondary, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
        content?.invoke()
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun FontSizeChip(label: String, active: Boolean, onClick: () -> Unit) {
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
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private fun fontScaleLabel(scale: Float): String = when (scale) {
    0.85f -> "صغير"
    1.0f -> "عادي"
    1.15f -> "كبير"
    1.3f -> "ضخم"
    else -> "عادي"
}
