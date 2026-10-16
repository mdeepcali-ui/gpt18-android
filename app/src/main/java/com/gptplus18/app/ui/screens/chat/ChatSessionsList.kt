package com.gptplus18.app.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.R
import com.gptplus18.app.data.models.AllSession
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.LocalAppColors
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextTertiary
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.shadow
import com.gptplus18.app.ui.theme.Info
import androidx.compose.foundation.combinedClickable

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatSessionsList — Sessions & Pinned Components
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.5
 * ═══════════════════════════════════════════════════════════════
 *
 *  قوائم المحادثات:
 *    • SessionsList         — قائمة محادثات الشات
 *    • PinnedMessagesBar    — شريط الرسائل المثبّتة
 *    • AllSessionsListView  — قائمة موحّدة (شات + كود)
 *
 *  ⚠️ جميع الدوال internal — تُستخدم داخل ChatScreen فقط
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SessionsList(
    sessions: List<Session>,
    onOpen: (Session) -> Unit,
    onLongPress: (Session) -> Unit,
) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.t_047), color = TextSecondary, fontSize = 10.6.sp)
                Text(stringResource(R.string.t_048), color = TextTertiary, fontSize = 9.1.sp,
                    modifier = Modifier.padding(top = 6.dp))
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
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = { onOpen(s) },
                        onLongClick = { onLongPress(s) },
                    ),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.title, color = TextPrimary, fontWeight = FontWeight.SemiBold,
                            fontSize = 10.6.sp, modifier = Modifier.weight(1f))
                        if (s.msgCount > 0) {
                            Text("${s.msgCount}", color = Accent, fontSize = 9.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                    s.lastMsg?.let {
                        Text(it.take(80), color = TextSecondary, fontSize = 9.1.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PinnedMessagesBar(
    messages: List<Message>,
    onUnpin: (Double) -> Unit,
) {
    val latest = messages.lastOrNull() ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14141A))
            .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(Accent, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\ud83d\udccc", fontSize = 9.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "\u0645\u062b\u0628\u0651\u062a",
                        color = Accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (messages.size > 1) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "+${messages.size - 1}",
                            color = TextSecondary,
                            fontSize = 9.sp,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    latest.content.take(80),
                    color = TextPrimary,
                    fontSize = 9.sp,
                    maxLines = 1,
                )
            }
            IconButton(
                onClick = { onUnpin(latest.ts) },
                modifier = Modifier.size(24.dp),
            ) {
                Text("\u2715", color = TextSecondary, fontSize = 9.sp)
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════
// 📋 AllSessionsListView — Chat + Code مع أيقونة نوع
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AllSessionsListView(
    sessions: List<AllSession>,
    onOpenChat: (AllSession) -> Unit,
    onOpenCode: (AllSession) -> Unit,
    onLongPressChat: (AllSession) -> Unit,
) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.t_047), color = TextSecondary, fontSize = 10.6.sp)
                Text(stringResource(R.string.t_048), color = TextTertiary, fontSize = 9.1.sp,
                    modifier = Modifier.padding(top = 6.dp))
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(sessions, key = { "${it.type}_${it.id}" }) { s ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = {
                            if (s.type == "code") onOpenCode(s)
                            else onOpenChat(s)
                        },
                        onLongClick = {
                            if (s.type == "chat") onLongPressChat(s)
                        },
                    ),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // أيقونة النوع
                    Icon(
                        if (s.type == "code") Icons.Default.Code else Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = if (s.type == "code") Info else Accent,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            s.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.6.sp,
                        )
                        s.lastMsg?.let {
                            Text(
                                it.take(80),
                                color = TextSecondary,
                                fontSize = 9.1.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
