package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachMenuSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
    onFiles: () -> Unit,
    onAddons: () -> Unit,
    onDeepThink: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            AttachItem(
                icon = Icons.Default.CameraAlt,
                label = "كاميرا",
                onClick = { onCamera(); onDismiss() },
            )
            AttachItem(
                icon = Icons.Default.Image,
                label = "الصور",
                onClick = { onPhotos(); onDismiss() },
            )
            AttachItem(
                icon = Icons.Default.InsertDriveFile,
                label = "ملفات",
                onClick = { onFiles(); onDismiss() },
            )
            AttachItem(
                icon = Icons.Default.Extension,
                label = "المكونات الإضافية",
                onClick = { onAddons(); onDismiss() },
            )
            AttachItem(
                icon = Icons.Default.Psychology,
                label = "فكّر بعمق أكبر",
                onClick = { onDeepThink(); onDismiss() },
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AttachItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right,
        )
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF3A3A3C), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                label,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
