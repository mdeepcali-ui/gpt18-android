package com.gptplus18.app.ui.screens.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.data.repository.UpdateInfo
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@Composable
fun UpdateDialog(
    info: UpdateInfo,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!info.forceUpdate) onDismiss() },
        icon = {
            Icon(
                Icons.Default.CloudDownload,
                stringResource(R.string.t_101),
                tint = Accent,
                modifier = Modifier.size(48.dp),
            )
        },
        title = {
            Text(
                if (info.forceUpdate) stringResource(R.string.t_222) else stringResource(R.string.t_223),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        },
        text = {
            Column {
                Text(
                    "الإصدار الجديد: ${info.latestName}",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "الإصدار الحالي: ${info.currentVersion}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
                if (info.changelog.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.t_089),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        info.changelog,
                        color = TextSecondary,
                        fontSize = 13.sp,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        ctx.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)),
                        )
                    } catch (_: Exception) { }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(stringResource(R.string.t_090), color = BgPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = if (!info.forceUpdate) {
            {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.t_091), color = TextSecondary)
                }
            }
        } else null,
        containerColor = BgSecondary,
    )
}
