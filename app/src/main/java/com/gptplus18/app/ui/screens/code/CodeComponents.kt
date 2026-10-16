package com.gptplus18.app.ui.screens.code

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.TextTertiary
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import com.gptplus18.app.data.models.CodeFile
import com.gptplus18.app.data.models.CodeSession
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary

/**
 * ═══════════════════════════════════════════════════════════════
 *  CodeComponents — Small Reusable Composables
 *  UXRD-2026-10-16-001 · Phase 4 · D-4.1
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
internal fun FilesDownloadSection(
    files: List<com.gptplus18.app.data.models.CodeFile>,
    zipUrl: String?,
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.material3.Surface(
        color = BgSecondary,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, TextSecondary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "📥 الملفات الجاهزة",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 9.8.sp,
            )
            Spacer(Modifier.height(8.dp))

            // ZIP أولاً
            if (zipUrl != null) {
                androidx.compose.material3.Button(
                    onClick = {
                        try {
                            val i = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(zipUrl))
                            ctx.startActivity(i)
                        } catch (_: Exception) {}
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Accent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("📦 تحميل الكل (ZIP)", color = androidx.compose.ui.graphics.Color.White)
                }
                Spacer(Modifier.height(6.dp))
            }

            // ملفات فردية
            files.forEach { f ->
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            try {
                                val i = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(f.url))
                                ctx.startActivity(i)
                            } catch (_: Exception) {}
                        },
                ) {
                    Text("📄", fontSize = 11.3.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        f.name,
                        color = TextPrimary,
                        fontSize = 9.1.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${f.size / 1024} KB",
                        color = TextSecondary,
                        fontSize = 9.sp,
                    )
                }
            }
        }
    }
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
internal fun SessionList(
    sessions: List<CodeSession>,
    onOpen: (CodeSession) -> Unit,
    onLongPress: (CodeSession) -> Unit,
) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.t_051), color = Accent, fontSize = 14.1.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.t_053), color = TextSecondary, fontSize = 9.1.sp,
                    modifier = Modifier.padding(top = 8.dp))
                Text(stringResource(R.string.t_054), color = TextTertiary, fontSize = 9.sp,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(sessions, key = { it.id }) { s ->
            Card(
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = { onOpen(s) },
                    onLongClick = { onLongPress(s) },
                ),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(s.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 10.6.sp)
                    s.lastRequest?.let {
                        Text(it.take(80), color = TextSecondary, fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RunningIndicator(
    status: String,
    logs: List<com.gptplus18.app.data.models.CodeLogEntry> = emptyList(),
    thinkingText: String = "",
) {
    // ☁️ سحابة التفكير مع Shimmer
    val statusText = when (status) {
        "starting" -> stringResource(R.string.t_163)
        "running" -> stringResource(R.string.t_164)
        else -> status
    }
    val logsText = when {
        thinkingText.isNotBlank() -> thinkingText.trim()
        logs.isNotEmpty() -> logs.takeLast(4).joinToString("\n") { "• ${it.msg}" }
        else -> ""  // ⭐ نعرض 3 نقاط بدل النص
    }
    com.gptplus18.app.ui.components.ThinkingShimmer(
        text = logsText,
        modifier = Modifier.fillMaxWidth(),
    )
}




