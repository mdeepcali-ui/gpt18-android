package com.gptplus18.app.ui.screens.chat

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import com.gptplus18.app.util.AutoRouter
import com.gptplus18.app.ui.components.AppDrawerContent
import com.gptplus18.app.ui.components.AddonsSheet
import com.gptplus18.app.ui.components.AttachMenuSheet
import com.gptplus18.app.ui.components.DeepThinkSheet
import com.gptplus18.app.ui.components.ChatGptComposer
import com.gptplus18.app.ui.components.FullscreenImageViewer
import com.gptplus18.app.ui.components.MarkdownText
import com.gptplus18.app.ui.components.MessageActionsSheet
import com.gptplus18.app.ui.components.MessageTimestamp
import com.gptplus18.app.ui.components.ModeSelector
import com.gptplus18.app.ui.components.ThinkingProcess
import com.gptplus18.app.ui.components.TypingIndicator
import com.gptplus18.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    onNavigateToCode: () -> Unit = {},
    onNavigateToMedia: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    vm: ChatViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()
    val clip = LocalClipboardManager.current

    var input by remember { mutableStateOf("") }
    var deleteDialogFor by remember { mutableStateOf<Session?>(null) }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showAddonsSheet by remember { mutableStateOf(false) }
    var showDeepThinkSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var fullscreenImage by remember { mutableStateOf<String?>(null) }
    var actionsSheetFor by remember { mutableStateOf<Message?>(null) }

    // ─── Voice Input ───
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) input = text
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(
                    android.speech.RecognizerIntent.EXTRA_LANGUAGE,
                    java.util.Locale.getDefault().toString(),
                )
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "تحدث الآن...")
            }
            try {
                voiceLauncher.launch(intent)
            } catch (_: Exception) { }
        }
    }

    // ─── Camera Launcher ───
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                val size = getFileSize(ctx, uri)
                vm.addAttachment(uri, "image/jpeg", "camera_${System.currentTimeMillis()}.jpg", size)
            }
        }
    }

    fun launchCamera() {
        try {
            val file = java.io.File(
                ctx.cacheDir,
                "cam_${System.currentTimeMillis()}.jpg"
            )
            val uri = androidx.core.content.FileProvider.getUriForFile(
                ctx,
                "${ctx.packageName}.fileprovider",
                file,
            )
            cameraUri = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) { }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val mime = ctx.contentResolver.getType(it) ?: "image/jpeg"
            val name = "image_${System.currentTimeMillis()}.jpg"
            val size = getFileSize(ctx, it)
            vm.addAttachment(it, mime, name, size)
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val mime = ctx.contentResolver.getType(it) ?: "application/octet-stream"
            val name = "file_${System.currentTimeMillis()}"
            val size = getFileSize(ctx, it)
            vm.addAttachment(it, mime, name, size)
        }
    }

    LaunchedEffect(state.messages.size, state.isSending, state.isUploading) {
        val total = state.messages.size + if (state.isSending || state.isUploading) 1 else 0
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName = state.userName,
                userEmail = "",
                isOwner = state.isOwner,
                onNewChat = { vm.newChat(); scope.launch { drawerState.close() } },
                onChat = { scope.launch { drawerState.close() } },
                onCode = { scope.launch { drawerState.close() }; onNavigateToCode() },
                onMedia = { scope.launch { drawerState.close() }; onNavigateToMedia() },
                onSubscription = { scope.launch { drawerState.close() }; onNavigateToSubscription() },
                onProfile = { scope.launch { drawerState.close() }; onNavigateToProfile() },
                onAdmin = { scope.launch { drawerState.close() }; onNavigateToAdmin() },
                onLanguageToggle = { scope.launch { drawerState.close() } },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onNavigateToAuth()
                },
            )
        },
    ) {
        Scaffold(
            containerColor = BgPrimary,
            topBar = {
                if (showSearch && state.currentSessionId == null) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it; vm.setSearch(it) },
                                placeholder = { Text("ابحث...", color = TextTertiary) },
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
                            IconButton(onClick = { showSearch = false; searchQuery = ""; vm.setSearch("") }) {
                                Icon(Icons.Default.Close, "إغلاق", tint = Accent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
                    )
                } else {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (state.currentSessionId == null) {
                                    Text("محادثاتك", color = TextPrimary, fontWeight = FontWeight.Bold)
                                } else {
                                    ModeSelector(
                                        currentMode = state.currentMode,
                                        isSubscribed = state.isSubscribed,
                                        onModeSelected = { vm.setMode(it) },
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            if (state.currentSessionId != null) {
                                IconButton(onClick = { vm.backToList() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = Accent)
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "القائمة", tint = Accent)
                                }
                            }
                        },
                        actions = {
                            if (state.currentSessionId == null) {
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(Icons.Default.Search, "بحث", tint = Accent)
                                }
                                IconButton(onClick = { vm.newChat() }) {
                                    Icon(Icons.Default.Add, "جديدة", tint = Accent)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
                    )
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (state.currentSessionId == null) {
                    var isRefreshing by remember { mutableStateOf(false) }
                    LaunchedEffect(state.sessions) { isRefreshing = false }
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { isRefreshing = true; vm.loadSessions() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        SessionsList(
                            sessions = if (searchQuery.isBlank()) state.sessions else state.filteredSessions,
                            onOpen = { vm.openSession(it.id) },
                            onLongPress = { deleteDialogFor = it },
                        )
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                        ) {
                            items(state.messages, key = { it.id.toString() + it.ts }) { msg ->
                                MessageBubble(
                                    msg = msg,
                                    onImageClick = { url -> fullscreenImage = url },
                                    onLongPress = { actionsSheetFor = msg },
                                    onCopy = { text -> clip.setText(AnnotatedString(text)) },
                                    onEdit = { m -> input = m.content },
                                )
                            }
                            if (state.isSending || state.isUploading) {
                                item {
                                    Column(
                                        Modifier.fillMaxWidth().padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        TypingIndicator(
                                            type = when (state.statusLabel) {
                                                "يحلل" -> "analyze"
                                                "ينشئ" -> "create"
                                                "يكتب" -> "write"
                                                else -> "think"
                                            }
                                        )
                                        // نعرض التفكير الحقيقي إذا موجود
                                        val lastThinking = state.thinkingByMessage.values.lastOrNull()
                                        if (lastThinking != null) {
                                            ThinkingProcess(
                                                status = state.statusLabel,
                                                steps = lastThinking.steps,
                                                rawText = lastThinking.rawText,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        state.replyTo?.let { r ->
                            ReplyBar(content = r.content, onCancel = { vm.setReplyTo(null) })
                        }

                        ChatGptComposer(
                            value = input,
                            onValueChange = { input = it },
                            enabled = !state.isSending && !state.isUploading,
                            attachments = state.pendingAttachments,
                            onAttachClick = { showAttachSheet = true },
                            onVoiceClick = {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            onSend = {
                                // ─── Auto-Routing ───
                                if (input.isNotBlank()) {
                                    val suggested = AutoRouter.suggestMode(input)
                                    // فقط إذا كان مختلف عن الحالي ومش CHAT
                                    if (suggested != state.currentMode && suggested != com.gptplus18.app.data.models.ChatMode.CHAT) {
                                        vm.setMode(suggested)
                                    }
                                }

                                if (state.pendingAttachments.isNotEmpty()) {
                                    vm.sendWithAttachments(input)
                                } else if (input.isNotBlank()) {
                                    vm.send(input)
                                }
                                input = ""
                            },
                            onRemoveAttachment = { vm.removeAttachment(it) },
                        )
                    }
                }
            }
        }
    }

    if (showAttachSheet) {
        AttachMenuSheet(
            onDismiss = { showAttachSheet = false },
            onCamera = { launchCamera() },
            onPhotos = { imagePicker.launch("image/*") },
            onFiles = { filePicker.launch("*/*") },
            onAddons = { showAddonsSheet = true },
            onDeepThink = { showDeepThinkSheet = true },
        )
    }

    if (showAddonsSheet) {
        AddonsSheet(
            onDismiss = { showAddonsSheet = false },
            onSelect = { text -> input = text },
        )
    }

    if (showDeepThinkSheet) {
        DeepThinkSheet(
            onDismiss = { showDeepThinkSheet = false },
            onSelect = { text -> input = text },
        )
    }

    deleteDialogFor?.let { s ->
        AlertDialog(
            onDismissRequest = { deleteDialogFor = null },
            title = { Text("حذف المحادثة", color = TextPrimary) },
            text = { Text("متأكد بدك تحذف \"${s.title}\"؟", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { vm.deleteSession(s.id); deleteDialogFor = null }) {
                    Text("حذف", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogFor = null }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = BgSecondary,
        )
    }

    actionsSheetFor?.let { msg ->
        MessageActionsSheet(
            msg = msg,
            onCopy = { clip.setText(AnnotatedString(msg.content)) },
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, msg.content)
                }
                ctx.startActivity(Intent.createChooser(intent, "مشاركة"))
            },
            onReply = { vm.setReplyTo(msg) },
            onDelete = null,
            onDismiss = { actionsSheetFor = null },
        )
    }

    fullscreenImage?.let { url ->
        FullscreenImageViewer(imageUrl = url, onDismiss = { fullscreenImage = null })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionsList(
    sessions: List<Session>,
    onOpen: (Session) -> Unit,
    onLongPress: (Session) -> Unit,
) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ما في محادثات بعد", color = TextSecondary, fontSize = 15.sp)
                Text("اضغط + لبدء محادثة", color = TextTertiary, fontSize = 13.sp,
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
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = { onOpen(s) },
                    onLongClick = { onLongPress(s) },
                ),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.title, color = TextPrimary, fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp, modifier = Modifier.weight(1f))
                        if (s.msgCount > 0) {
                            Text("${s.msgCount}", color = Accent, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                    s.lastMsg?.let {
                        Text(it.take(80), color = TextSecondary, fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    msg: Message,
    onImageClick: (String) -> Unit,
    onLongPress: () -> Unit,
    onCopy: (String) -> Unit,
    onEdit: (Message) -> Unit,
) {
    val isUser = msg.role == "user"
    val imageUrl = MessageHelpers.extractImageUrl(msg.content)
    val audioUrl = MessageHelpers.extractAudioUrl(msg.content)
    val cleanText = MessageHelpers.stripMediaMarkers(msg.content)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Surface(
                color = if (isUser) Color(0xFF2F2F2F) else Color(0xFF1E1E1E),
                shape = RoundedCornerShape(
                    topStart = 20.dp, topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 6.dp,
                    bottomEnd = if (isUser) 6.dp else 20.dp,
                ),
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = onLongPress,
                ),
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "صورة",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .combinedClickable(
                                    onClick = { onImageClick(imageUrl) },
                                    onLongClick = onLongPress,
                                ),
                        )
                        if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                    }

                    if (audioUrl != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) {
                            Text("🎵", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("مقطع صوتي",
                                color = if (isUser) Color.White else TextPrimary,
                                fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                    }

                    if (cleanText.isNotBlank()) {
                        if (isUser) {
                            Text(cleanText, color = Color.White, fontSize = 16.sp)
                        } else {
                            MarkdownText(cleanText, textColor = TextPrimary, fontSize = 16)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                MessageTimestamp(msg.ts)

                if (!isUser) {
                    ActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "نسخ",
                        onClick = { onCopy(cleanText) },
                    )
                } else {
                    ActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "نسخ",
                        onClick = { onCopy(cleanText) },
                    )
                    ActionButton(
                        icon = Icons.Default.Edit,
                        label = "تعديل",
                        onClick = { onEdit(msg) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, label, tint = TextTertiary, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(3.dp))
        Text(label, color = TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun ReplyBar(content: String, onCancel: () -> Unit) {
    Surface(color = BgSecondary) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(Accent, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("رد على", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(content.take(60), color = TextSecondary, fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, "إلغاء", tint = TextSecondary,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun getFileSize(ctx: android.content.Context, uri: Uri): Long {
    return try {
        ctx.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
    } catch (_: Exception) { 0L }
}
