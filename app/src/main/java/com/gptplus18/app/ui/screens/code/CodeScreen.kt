@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.gptplus18.app.ui.screens.code

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
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
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.data.models.CodeMessage
import com.gptplus18.app.data.models.CodeSession
import com.gptplus18.app.ui.components.ChatGptComposer
import com.gptplus18.app.ui.components.MarkdownText
import com.gptplus18.app.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CodeScreen(vm: CodeViewModel = hiltViewModel()) {

    // X2: viewer
    var fullscreenImage by remember { mutableStateOf<String?>(null) }
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
                        Text(stringResource(R.string.t_051), color = TextPrimary, fontWeight = FontWeight.Bold)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Accent, RoundedCornerShape(50)),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.t_159),
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (state.currentSessionId != null) {
                        IconButton(onClick = { vm.backToList() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.t_092), tint = Accent)
                        }
                    }
                },
                actions = {
                    // ⭐ زر + دائري في الزاوية اليسرى
                    IconButton(
                        onClick = { vm.newRequest() },
                        modifier = Modifier
                            .padding(end = 8.dp, top = 2.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(1.2.dp, TextSecondary, CircleShape),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            stringResource(R.string.t_160),
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp),
                        )
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
                            CodeMessageItem(m) { url -> fullscreenImage = url }
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
            title = { Text(stringResource(R.string.t_052), color = TextPrimary) },
            text = { Text("حذف \"${s.title}\"؟", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSession(s.id)
                    deleteDialogFor = null
                }) { Text(stringResource(R.string.t_046), color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogFor = null }) {
                    Text(stringResource(R.string.t_026), color = TextSecondary)
                }
            },
            containerColor = BgSecondary,
        )
    }
    
    // X2: viewer
    fullscreenImage?.let { url ->
        com.gptplus18.app.ui.components.FullscreenImageViewer(
            imageUrl = url,
            onDismiss = { fullscreenImage = null },
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
                Text(stringResource(R.string.t_051), color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.t_053), color = TextSecondary, fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp))
                Text(stringResource(R.string.t_054), color = TextTertiary, fontSize = 12.sp,
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
private fun CodeMessageItem(m: CodeMessage, onImageClick: (String) -> Unit = {}) {
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
                    if (isUser) stringResource(R.string.t_161) else stringResource(R.string.t_162),
                    color = if (isUser) Accent else Success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { clip.setText(AnnotatedString(m.content)) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.ContentCopy, stringResource(R.string.t_003), tint = TextTertiary,
                        modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            MarkdownText(
                text = m.content,
                onImageClick = onImageClick,
                textColor = TextPrimary,
                fontSize = 13,
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
                        "starting" -> stringResource(R.string.t_163)
                        "running" -> stringResource(R.string.t_164)
                        else -> stringResource(R.string.t_165)
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




