package com.gptplus18.app.ui.screens.code

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.R
import com.gptplus18.app.ui.screens.chat.components.RunButton
import com.gptplus18.app.ui.screens.chat.components.SaveButton
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary

/**
 * ═══════════════════════════════════════════════════════════════
 *  CodeHeader — TopAppBar Composable
 *  UXRD-2026-10-16-001 · Phase 4 · D-4.2
 * ═══════════════════════════════════════════════════════════════
 *
 *  الشريط العلوي لشاشة البرمجة:
 *    • القائمة (Drawer)
 *    • الدردشة (رجوع)
 *    • SAVE / RUN
 *    • + (طلب جديد)
 *
 *  ⚠️ internal — تُستخدم من CodeScreen فقط
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CodeHeader(
    isRunning: Boolean,
    onDrawerOpen: () -> Unit,
    onNavigateToChat: () -> Unit,
    onRunClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNewRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSecondary)
            .statusBarsPadding(),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.t_051),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onDrawerOpen,
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "القائمة",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            actions = {
                // 💬 زر الرجوع للدردشة
                IconButton(
                    onClick = onNavigateToChat,
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "الدردشة",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                // 💾 زر SAVE
                SaveButton(
                    onClick = onSaveClick,
                    enabled = !isRunning,
                )
                Spacer(Modifier.width(12.dp))
                // ▶️ زر RUN
                RunButton(
                    onClick = onRunClick,
                    enabled = !isRunning,
                )
                Spacer(Modifier.width(12.dp))
                // ⭐ زر + دائري
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp, top = 2.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, TextPrimary, CircleShape)
                        .clickable { onNewRequest() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Add,
                        stringResource(R.string.t_160),
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            modifier = Modifier.height(40.dp),
            windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        )
    }
}
