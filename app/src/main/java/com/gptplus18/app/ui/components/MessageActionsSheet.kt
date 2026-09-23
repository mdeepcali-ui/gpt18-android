package com.gptplus18.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionsSheet(
    msg: Message,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReply: () -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgSecondary,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text(
                stringResource(R.string.t_004),
                color = Accent,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            ActionRow("نسخ", Icons.Default.ContentCopy, Accent) {
                onCopy(); onDismiss()
            }
            ActionRow("مشاركة", Icons.Default.Share, Accent) {
                onShare(); onDismiss()
            }
            if (msg.role == "user") {
                ActionRow("رد", Icons.Default.Reply, Accent) {
                    onReply(); onDismiss()
                }
            }
            if (onDelete != null) {
                ActionRow("حذف", Icons.Default.Delete, Color(0xFFEF4444)) {
                    onDelete(); onDismiss()
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Icon(icon, label, tint = tint)
            Spacer(Modifier.width(16.dp))
            Text(label, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
