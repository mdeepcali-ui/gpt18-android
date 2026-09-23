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
import androidx.compose.ui.graphics.Color
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
                  subtitle = when {
                      state.notificationsLoading -> "جاري التفعيل..."
                      state.notificationsEnabled -> "مفعّلة — رح توصلك تنبيهات"
                      else -> "معطّلة"
                  },
              ) {
                  if (state.notificationsLoading) {
                      CircularProgressIndicator(
                          modifier = Modifier.size(22.dp),
                          color = Accent,
                          strokeWidth = 2.dp,
                      )
                  } else {
                      Switch(
                          checked = state.notificationsEnabled,
                          onCheckedChange = { vm.setNotificationsEnabled(it) },
                          colors = SwitchDefaults.colors(checkedTrackColor = Accent),
                      )
                  }
              }

              // خطأ الإشعارات
              state.notificationsError?.let { err ->
                  Row(
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 4.dp, vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                      Text(
                          text = "⚠️ $err",
                          color = Color(0xFFFF6B6B),
                          fontSize = 12.sp,
                          modifier = Modifier.weight(1f),
                      )
                      TextButton(onClick = { vm.dismissNotificationError() }) {
                          Text("حسناً", color = Accent, fontSize = 12.sp)
                      }
                  }
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
            .background(BgSecondary, RoundedCornerShape(20.dp))
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
private fun FontSizeChip(
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
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
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
