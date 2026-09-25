package com.gptplus18.app.ui.screens.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.ui.theme.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.io.ByteArrayOutputStream
import com.gptplus18.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onAdminClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.t_069), color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            // Avatar + زر تغيير
            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()

            val pickLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    try {
                        val input = ctx.contentResolver.openInputStream(uri)
                        val bmp = BitmapFactory.decodeStream(input)
                        input?.close()
                        if (bmp != null) {
                            // ضغط + resize إلى 256×256
                            val maxSize = 256
                            val ratio = minOf(maxSize.toFloat() / bmp.width, maxSize.toFloat() / bmp.height, 1f)
                            val newW = (bmp.width * ratio).toInt().coerceAtLeast(1)
                            val newH = (bmp.height * ratio).toInt().coerceAtLeast(1)
                            val scaled = Bitmap.createScaledBitmap(bmp, newW, newH, true)
                            val baos = ByteArrayOutputStream()
                            scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                            val bytes = baos.toByteArray()
                            val b64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                            vm.setAvatar(b64)
                        }
                    } catch (_: Exception) {}
                }
            }

            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Accent)
                        .clickable { pickLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    val avatarModel = remember(state.avatarUrl) {
                        if (state.avatarUrl.startsWith("data:image")) {
                            // نفك base64 يدوياً
                            try {
                                val base64Part = state.avatarUrl.substringAfter(",")
                                val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) { null }
                        } else if (state.avatarUrl.isNotBlank()) {
                            state.avatarUrl
                        } else null
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
                            state.userName.take(1).uppercase().ifEmpty { "?" },
                            fontSize = 42.sp, fontWeight = FontWeight.Bold, color = BgPrimary,
                        )
                    }
                }
                // شارة كاميرا صغيرة
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Accent)
                        .clickable { pickLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📷", fontSize = 14.sp)
                }
            }

            if (state.isUploadingAvatar) {
                Spacer(Modifier.height(6.dp))
                Text("⏳ جاري الرفع...", color = Accent, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))
            Text(state.userName.ifEmpty { stringResource(R.string.t_196) }, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(state.userEmail, color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))

            // Subscription status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.t_070), color = Accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            state.hasSubscription -> stringResource(R.string.t_197)
                            state.hasTrial -> stringResource(R.string.t_084)
                            else -> stringResource(R.string.t_085)
                        },
                        color = TextPrimary, fontSize = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Admin Button
            if (state.isOwner) {
                Card(
                    onClick = onAdminClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Accent.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Accent)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.t_071), color = Accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.weight(1f))
                        Text("→", color = Accent, fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ═══ Dark mode ═══
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.DarkMode, null, tint = Accent)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.t_072), color = TextPrimary, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.darkMode,
                    onCheckedChange = { vm.toggleDarkMode(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent),
                )
            }

            // ═══ Language ═══
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Language, null, tint = Accent)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.t_073), color = TextPrimary, modifier = Modifier.weight(1f))
                TextButton(onClick = { vm.toggleLanguage() }) {
                    Text(if (state.language == "ar") stringResource(R.string.t_198) else "English", color = Accent)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ═══ Font Size ═══
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.FormatSize, null, tint = Accent)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.t_074), color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(
                        text = fontScaleLabel(state.fontScale),
                        color = Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FontSizeOption(
                        label = stringResource(R.string.t_199),
                        sample = "Aa",
                        sampleSize = 13.sp,
                        selected = state.fontScale == 0.85f,
                        onClick = { vm.setFontScale(0.85f) },
                        modifier = Modifier.weight(1f),
                    )
                    FontSizeOption(
                        label = stringResource(R.string.t_200),
                        sample = "Aa",
                        sampleSize = 15.sp,
                        selected = state.fontScale == 1.0f,
                        onClick = { vm.setFontScale(1.0f) },
                        modifier = Modifier.weight(1f),
                    )
                    FontSizeOption(
                        label = stringResource(R.string.t_201),
                        sample = "Aa",
                        sampleSize = 17.sp,
                        selected = state.fontScale == 1.15f,
                        onClick = { vm.setFontScale(1.15f) },
                        modifier = Modifier.weight(1f),
                    )
                    FontSizeOption(
                        label = stringResource(R.string.t_202),
                        sample = "Aa",
                        sampleSize = 19.sp,
                        selected = state.fontScale == 1.3f,
                        onClick = { vm.setFontScale(1.3f) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(6.dp))

                // معاينة
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgSecondary),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.t_075),
                        color = TextSecondary,
                        fontSize = (15 * state.fontScale).sp,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══ Settings ═══
            Card(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(14.dp),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Settings, null, tint = Accent)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.t_076), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text("→", color = Accent, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ═══ Logout ═══
            Button(
                onClick = {
                    vm.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Logout, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.t_077), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FontSizeOption(
    label: String,
    sample: String,
    sampleSize: androidx.compose.ui.unit.TextUnit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Accent else BgSecondary,
        ),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            TextSecondary.copy(alpha = 0.2f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = sample,
                fontSize = sampleSize,
                color = if (selected) BgPrimary else TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (selected) BgPrimary else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        }
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
