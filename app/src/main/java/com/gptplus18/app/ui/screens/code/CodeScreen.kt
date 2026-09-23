@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.gptplus18.app.ui.screens.code

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.data.models.CodeMessage
import com.gptplus18.app.data.models.CodeModel
import com.gptplus18.app.data.models.CodeSession
import com.gptplus18.app.ui.components.ChatGptComposer
import com.gptplus18.app.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CodeScreen(vm: CodeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    var deleteDialogFor by remember { mutableStateOf<CodeSession?>(null) }

    LaunchedEffect(state.messages.size, state.isRunning) {
        val total = state.messages.size + if (state.isRunning) 1 else 0
        if (total > 0) listState.scrollToItem(total - 1)
    }

    // 🆕 عند فتح الشاشة — افتح جلسة برمجة جديدة تلقائياً
    LaunchedEffect(Unit) {
        if (state.currentSessionId == null) {
            vm.newRequest()
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = {
                    if (state.currentSessionId == null) {
                        Text("وضع البرمجة", color = TextPrimary, fontWeight = FontWeight.Bold)
                    } else {
                        CodeModelSelector(
                            currentModel = state.currentModel,
                            onSelect = { vm.setModel(it) },
                        )
                    }
                },
                navigationIcon = {
                    if (state.currentSessionId != null) {
                        IconButton(onClick = { vm.backToList() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = Accent)
                        }
                    }
                },
                actions = {
                    if (state.currentSessionId == null) {
                        IconButton(onClick = { vm.newRequest() }) {
                            Icon(Icons.Default.Add, "جديد", tint = Accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.currentSessionId == null) {
                SessionList(
                    sessions = state.sessions,
                    onOpen = { vm.openSession(it.id) },
                    onLongPress = { deleteDialogFor = it },
                )
            } else {
                Column(Modifier.fillMaxSize().imePadding()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        items(state.messages, key = { it.id.toString() + it.ts }) { m ->
                            CodeMessageItem(m)
                        }
                        if (state.isRunning) {
                            item { RunningIndicator(state.jobStatus, state.logs) }
                        }
                    }
                    ChatGptComposer(
                        value = input,
                        onValueChange = { input = it },
                        enabled = !state.isRunning,
                        attachments = emptyList(),
                        onAttachClick = { /* TODO: attach in code mode */ },
                        onSend = {
                            if (input.isNotBlank()) {
                                vm.submit(input)
                                input = ""
                            }
                        },
                        onRemoveAttachment = { /* لا شي */ },
                    )
                }
            }
        }
    }

    deleteDialogFor?.let { s ->
        AlertDialog(
            onDismissRequest = { deleteDialogFor = null },
            title = { Text("حذف الجلسة", color = TextPrimary) },
            text = { Text("حذف \"${s.title}\"؟", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSession(s.id)
                    deleteDialogFor = null
                }) { Text("حذف", color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogFor = null }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = BgSecondary,
        )
    }
}

@Composable
private fun SessionList(
    sessions: List<CodeSession>,
    onOpen: (CodeSession) -> Unit,
    onLongPress: (CodeSession) -> Unit,
) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("وضع البرمجة", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("اكتب طلبك البرمجي ودع الفريق يبني", color = TextSecondary, fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp))
                Text("للمشتركين فقط", color = TextTertiary, fontSize = 12.sp,
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
                    Text(s.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    s.lastRequest?.let {
                        Text(it.take(80), color = TextSecondary, fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeMessageItem(m: CodeMessage) {
    val isUser = m.role == "user"
    val clip = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) Accent.copy(alpha = 0.12f) else BgSecondary,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isUser) "أنت" else "الفريق",
                    color = if (isUser) Accent else Success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { clip.setText(AnnotatedString(m.content)) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.ContentCopy, "نسخ", tint = TextTertiary,
                        modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                m.content,
                color = TextPrimary,
                fontSize = 13.sp,
                fontFamily = if (!isUser) FontFamily.Monospace else FontFamily.Default,
            )
        }
    }
}

@Composable
private fun RunningIndicator(status: String, logs: List<com.gptplus18.app.data.models.CodeLogEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = Accent,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    when (status) {
                        "starting" -> "جاري التحضير..."
                        "running" -> "الفريق يعمل..."
                        else -> "قيد التنفيذ..."
                    },
                    color = TextSecondary, fontSize = 13.sp,
                )
            }
            if (logs.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                logs.takeLast(4).forEach { log ->
                    Text(
                        "• ${log.msg}",
                        color = TextTertiary, fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp),
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodeModelSelector(
    currentModel: CodeModel,
    onSelect: (CodeModel) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .background(BgSecondary, RoundedCornerShape(20.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(currentModel.color, RoundedCornerShape(50)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                currentModel.label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = "اختر",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = BgSecondary,
            modifier = Modifier.width(300.dp),
        ) {
            Text(
                "اختر النموذج",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            CodeModel.values().forEach { m ->
                DropdownMenuItem(
                    text = {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(m.emoji, fontSize = 16.sp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    m.label,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.weight(1f))
                                if (m == currentModel) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = Accent,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                            Text(
                                m.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 26.dp, top = 2.dp),
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(m)
                    },
                )
            }
        }
    }
}
