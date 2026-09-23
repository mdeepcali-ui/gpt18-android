package com.gptplus18.app.ui.screens.settings

import com.gptplus18.app.BuildConfig
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.clickable
import android.net.Uri
import android.content.Intent
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
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

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
                title = { Text(stringResource(R.string.t_076), color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.t_092), tint = Accent)
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
            SectionTitle(stringResource(R.string.t_203))


                        Spacer(Modifier.height(10.dp))

            // ─── اللغة ───
            SectionTitle(stringResource(R.string.t_073))

            SettingRow(
                icon = Icons.Default.Language,
                title = stringResource(R.string.t_204),
                subtitle = if (state.language == "ar") stringResource(R.string.t_198) else "English",
            ) {
                TextButton(onClick = { vm.toggleLanguage() }) {
                    Text(if (state.language == "ar") stringResource(R.string.t_198) else "English", color = Accent, fontSize = 13.sp)
                }
            }

              // ─── الإشعارات ───
              SectionTitle(stringResource(R.string.t_205))

              SettingRow(
                  icon = Icons.Default.Notifications,
                  title = stringResource(R.string.t_206),
                  subtitle = when {
                      state.notificationsLoading -> stringResource(R.string.t_207)
                      state.notificationsEnabled -> stringResource(R.string.t_208)
                      else -> stringResource(R.string.t_209)
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
                          Text(stringResource(R.string.t_078), color = Accent, fontSize = 12.sp)
                      }
                  }
              }

            // ─── أخرى ───
            SectionTitle(stringResource(R.string.t_210))

            SettingRow(
                icon = Icons.Default.Refresh,
                title = stringResource(R.string.t_211),
                subtitle = stringResource(R.string.t_212),
                onClick = { vm.checkUpdates() },
            )

            SettingRow(
                icon = Icons.Default.Info,
                title = stringResource(R.string.t_008),
                subtitle = stringResource(R.string.t_213),
                onClick = onAbout,
            )

            // ⭐ E1: التواصل مع الدعم عبر البريد
            val ctx = LocalContext.current
            SettingRow(
                icon = Icons.Default.Email,
                title = "التواصل مع الدعم",
                subtitle = "m.deep.cali@outlook.sa",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:m.deep.cali@outlook.sa")
                            putExtra(Intent.EXTRA_SUBJECT, "دعم GPT+18 — v${BuildConfig.VERSION_NAME}")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "\n\n\n---\nاسم المستخدم: ${state.userName}\nالإصدار: ${BuildConfig.VERSION_NAME}\nالمعرّف: ${state.uid}"
                            )
                        }
                        ctx.startActivity(intent)
                    } catch (e: Exception) {
                        // إذا لا يوجد تطبيق بريد → نفتح Gmail web
                        try {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/mail/?view=cm&to=m.deep.cali@outlook.sa"))
                            )
                        } catch (_: Exception) { }
                    }
                },
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
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

@androidx.compose.runtime.Composable
private fun fontScaleLabel(scale: Float): String = when (scale) {
    0.85f -> stringResource(R.string.t_199)
    1.0f -> stringResource(R.string.t_200)
    1.15f -> stringResource(R.string.t_201)
    1.3f -> stringResource(R.string.t_202)
    else -> stringResource(R.string.t_200)
}
