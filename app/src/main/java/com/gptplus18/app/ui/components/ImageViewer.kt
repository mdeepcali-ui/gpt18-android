package com.gptplus18.app.ui.components

import android.content.Intent
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@Composable
fun FullscreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
            )

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ⭐ Save
                IconButton(
                    onClick = {
                        try {
                            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val req = DownloadManager.Request(Uri.parse(imageUrl))
                            req.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_PICTURES,
                                "GPT+18/gpt18_${System.currentTimeMillis()}.jpg",
                            )
                            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            dm.enqueue(req)
                        } catch (_: Exception) { }
                        onDismiss()
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .size(44.dp),
                ) {
                    Icon(Icons.Default.Download, stringResource(R.string.t_232), tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                // ⭐ Share
                IconButton(
                    onClick = {
                        try {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/*"
                                putExtra(Intent.EXTRA_TEXT, imageUrl)
                            }
                            ctx.startActivity(Intent.createChooser(sendIntent, "مشاركة الصورة"))
                        } catch (_: Exception) { }
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .size(44.dp),
                ) {
                    Icon(Icons.Default.Share, stringResource(R.string.t_233), tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                // ⭐ Close
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .size(44.dp),
                ) {
                    Icon(Icons.Default.Close, stringResource(R.string.t_126), tint = Color.White)
                }
            }
        }
    }
}
