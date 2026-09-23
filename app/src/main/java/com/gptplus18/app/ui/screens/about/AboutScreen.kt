package com.gptplus18.app.ui.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    val ctx = LocalContext.current

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.t_008), color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Accent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "GPT+18",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Accent,
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "GPT+18",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.t_009),
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                stringResource(R.string.t_010),
                color = TextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(32.dp))

            // وصف
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.t_011),
                        color = Accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.t_012),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // روابط
            SectionTitle(stringResource(R.string.t_093))

            LinkRow(stringResource(R.string.t_094), "@qesaasss", Icons.Default.Chat, "https://t.me/qesaasss")
            LinkRow(stringResource(R.string.t_095), "@qisaasss", Icons.Default.Chat, "https://t.me/qisaasss")
            LinkRow(stringResource(R.string.t_096), "@Qisas_Plus18_bot", Icons.Default.Send, "https://t.me/Qisas_Plus18_bot")
            LinkRow(stringResource(R.string.t_097), "gptplus18.com", Icons.Default.Language, "https://gptplus18.com")

            Spacer(Modifier.height(24.dp))
            SectionTitle(stringResource(R.string.t_098))

            LinkRow(stringResource(R.string.t_099), "", Icons.Default.PrivacyTip, "https://gptplus18.com/terms")
            LinkRow(stringResource(R.string.t_100), "", Icons.Default.PrivacyTip, "https://gptplus18.com/privacy")

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.t_013),
                color = TextTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.t_014),
                color = TextTertiary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(t: String) {
    Text(
        t,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp).fillMaxWidth(),
    )
}

@Composable
private fun LinkRow(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    url: String,
) {
    val ctx = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSecondary, RoundedCornerShape(12.dp))
            .clickable {
                try {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (_: Exception) {}
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 14.sp)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextSecondary, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
        Text("→", color = Accent, fontSize = 16.sp)
    }
    Spacer(Modifier.height(8.dp))
}
