package com.gptplus18.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.R
import com.gptplus18.app.data.models.ChatMode
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextTertiary

/**
 * ═══════════════════════════════════════════════════════════════
 *  ChatHeader — TopAppBar Composable
 *  UXRD-2026-10-16-001 · Phase 2 · D-2.9
 * ═══════════════════════════════════════════════════════════════
 *
 *  الشريط العلوي للشات — يعرض:
 *    • وضع البحث (عند تفعيله وما في جلسة)
 *    • اسم الوضع الحالي (Chat / Code)
 *    • زر القائمة (Drawer)
 *    • زر البرمجة
 *    • زر محادثة جديدة (+)
 *    • زر البحث (عند عرض قائمة الجلسات)
 *
 *  ⚠️ internal — تُستخدم من ChatScreen فقط
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatHeader(
    state: ChatUiState,
    showSearch: Boolean,
    searchQuery: String,
    showSessionsList: Boolean,
    onSearchChange: (String) -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    onModeSelect: (ChatMode) -> Unit,
    onDrawerOpen: () -> Unit,
    onNewChat: () -> Unit,
    onNavigateToCode: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BgPrimary)
            .statusBarsPadding()
    ) {
        if (showSearch && state.currentSessionId == null) {
            // ═══ وضع البحث ═══
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(stringResource(R.string.t_043), color = TextTertiary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Accent,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onSearchClose) {
                        Icon(Icons.Default.Close, stringResource(R.string.t_126), tint = Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgPrimary),
                modifier = Modifier.height(44.dp),
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            )
        } else {
            // ═══ الوضع العادي ═══
            TopAppBar(
                title = {
                    if (showSessionsList) {
                        Text(
                            stringResource(R.string.t_044),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 17.sp,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ModeDropdown(
                                current = state.currentMode,
                                onSelect = onModeSelect,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDrawerOpen) {
                        Icon(Icons.Default.Menu, stringResource(R.string.t_127), tint = TextPrimary)
                    }
                },
                actions = {
                    // 💻 زر البرمجة
                    IconButton(onClick = { onNavigateToCode(-1) }) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = "البرمجة",
                            tint = Accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    // ⭐ زر + دائري في الزاوية اليسرى
                    Box(
                        modifier = Modifier
                            .padding(end = 18.dp, top = 2.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(3.0.dp, TextPrimary, CircleShape)
                            .clickable { onNewChat() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            stringResource(R.string.new_chat),
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    if (showSessionsList) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onSearchOpen,
                            modifier = Modifier.padding(end = 14.dp),
                        ) {
                            Icon(Icons.Default.Search, stringResource(R.string.t_128), tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgPrimary),
                modifier = Modifier.height(44.dp),
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            )
        }
    }
}
