package com.gptplus18.app.ui.screens.chat

import androidx.compose.material3.CircularProgressIndicator
import com.gptplus18.app.ui.theme.LocalAppColors
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gptplus18.app.data.models.ChatMode
import com.gptplus18.app.data.models.AllSession
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import com.gptplus18.app.ui.components.AddonsSheet
import com.gptplus18.app.ui.components.AppDrawerContent
import com.gptplus18.app.ui.components.AttachMenuSheet
import com.gptplus18.app.ui.components.ChatGptComposer
import com.gptplus18.app.ui.components.DeepThinkSheet
import com.gptplus18.app.ui.components.FullscreenImageViewer
import com.gptplus18.app.ui.components.MarkdownText
import com.gptplus18.app.ui.components.MessageActionsSheet
import com.gptplus18.app.ui.components.MessageTimestamp
import com.gptplus18.app.ui.components.TypingIndicator
import com.gptplus18.app.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    onNavigateToCode: () -> Unit = {},
    onNavigateToMedia: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
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
    // 📋 قائمة المحادثات — تُفتح من Drawer فقط
    var showSessionsList by remember { mutableStateOf(false) }

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
            val file = java.io.File(ctx.cacheDir, "cam_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                ctx, "${ctx.packageName}.fileprovider", file,
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

    LaunchedEffect(state.messages.size, state.isSending, state.isUploading, state.messages.lastOrNull()?.content?.length) {
        val total = state.messages.size + if (state.isSending || state.isUploading) 1 else 0
        if (total > 0) listState.scrollToItem(total - 1)
    }

    // 🚪 BackHandler — من سجل المحادثات → للشات، ومن الشات → خروج
    BackHandler(enabled = showSessionsList) {
        showSessionsList = false
    }

    // 🆕 عند فتح الشاشة لأول مرة — افتح شات جديدة دائماً
    LaunchedEffect(Unit) {
        if (state.currentSessionId == null) {
            vm.newChat()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName = state.userName,
                userEmail = "",
                isOwner = state.isOwner,
                onNewChat = {
                    vm.newChat()
                    showSessionsList = false
                    scope.launch { drawerState.close() }
                },
                onSessionsClick = {
                    showSessionsList = true
                    scope.launch { drawerState.close() }
                },
                lastSessionTitle = state.sessions.firstOrNull()?.title,
                onChat = { scope.launch { drawerState.close() } },
                onCode = { scope.launch { drawerState.close() }; onNavigateToCode() },
                onMedia = { scope.launch { drawerState.close() }; onNavigateToMedia() },
                onSubscription = { scope.launch { drawerState.close() }; onNavigateToSubscription() },
                onProfile = { scope.launch { drawerState.close() }; onNavigateToProfile() },
                onSettings = { scope.launch { drawerState.close() }; onNavigateToSettings() },
                onAdmin = { scope.launch { drawerState.close() }; onNavigateToAdmin() },
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
                                placeholder = { Text(stringResource(R.string.t_043), color = TextTertiary) },
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
                                Icon(Icons.Default.Close, stringResource(R.string.t_126), tint = Accent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgPrimary),
                    )
                } else {
                    TopAppBar(
                        title = {
                            if (showSessionsList) {
                                Text(stringResource(R.string.t_044), color = TextPrimary,
                                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ModeDropdown(
                                        current = state.currentMode,
                                        onSelect = { mode ->
                                            vm.setMode(mode)
                                            when (mode) {
                                                ChatMode.CODE -> onNavigateToCode()
                                                ChatMode.MEDIA -> onNavigateToMedia()
                                                else -> {}
                                            }
                                        },
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, stringResource(R.string.t_127), tint = TextPrimary)
                            }
                        },
                        actions = {
                            // ⭐ زر + دائري في الزاوية اليسرى
                            IconButton(
                                onClick = { vm.newChat() },
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, TextSecondary, CircleShape),
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    stringResource(R.string.new_chat),
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            if (showSessionsList) {
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(Icons.Default.Search, stringResource(R.string.t_128), tint = TextPrimary)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgPrimary),
                    )
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
            ) {
                if (showSessionsList) {
                    var isRefreshing by remember { mutableStateOf(false) }
                    LaunchedEffect(state.allSessions) { isRefreshing = false }
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { isRefreshing = true; vm.loadAllSessions() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        AllSessionsListView(
                            sessions = state.allSessions,
                            onOpenChat = {
                                vm.openSession(it.id)
                                showSessionsList = false
                            },
                            onOpenCode = {
                                onNavigateToCode()
                                showSessionsList = false
                            },
                            onLongPressChat = { deleteDialogFor = it.toChatSession() },
                        )
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            val visibleMessages = state.messages.filter { it.role != "thinking" }
                            items(visibleMessages, key = { it.id.toString() + it.ts }) { msg ->
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
                                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        TypingIndicator(
                                            type = when (state.statusLabel) {
                                                stringResource(R.string.t_129) -> "analyze"
                                                stringResource(R.string.t_130) -> "create"
                                                stringResource(R.string.t_131) -> "write"
                                                else -> "think"
                                            }
                                        )

                                        // 🎨 التفكير — نص رمادي صغير (يختفي لما الرد يبدأ)
                                        if (state.statusLabel != stringResource(R.string.t_131)) {
                                            val lastThinking = state.thinkingByMessage.values.lastOrNull()
                                            val thinkingText = buildString {
                                                val raw = lastThinking?.rawText?.trim().orEmpty()
                                                if (raw.isNotBlank()) {
                                                    append(raw)
                                                } else {
                                                    val steps = lastThinking?.steps.orEmpty()
                                                    if (steps.isNotEmpty()) {
                                                        append(steps.joinToString("\n") { "• $it" })
                                                    }
                                                }
                                            }
                                            if (thinkingText.isNotBlank()) {
                                                Text(
                                                    text = thinkingText,
                                                    color = TextTertiary,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp),
                                                )
                                            }
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
                            onSend = {
                                if (input.isNotBlank()) {
                                    val suggested = com.gptplus18.app.util.AutoRouter.suggestMode(input)
                                    if (suggested != state.currentMode && suggested != ChatMode.CHAT) {
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
            title = { Text(stringResource(R.string.t_045), color = TextPrimary) },
            text = { Text("متأكد بدك تحذف \"${s.title}\"؟", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { vm.deleteSession(s.id); deleteDialogFor = null }) {
                    Text(stringResource(R.string.t_046), color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogFor = null }) {
                    Text(stringResource(R.string.t_026), color = TextSecondary)
                }
            },
            containerColor = BgSecondary,
            shape = RoundedCornerShape(24.dp),
        )
    }

    actionsSheetFor?.let { msg ->
        MessageActionsSheet(
            msg = msg,
            onCopy = { clip.setText(AnnotatedString(msg.content)) },
            onShare = {
                val chooserTitle = ctx.getString(R.string.t_133)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, msg.content)
                }
                ctx.startActivity(Intent.createChooser(intent, chooserTitle))
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

@Composable
private fun ModeDropdown(
    current: ChatMode,
    onSelect: (ChatMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current
    val (label, icon) = when (current) {
        ChatMode.CODE -> "Code" to Icons.Default.Code
        ChatMode.MEDIA -> "Media" to Icons.Default.Movie
        else -> "Chat" to Icons.AutoMirrored.Filled.Chat
    }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Icon(Icons.Default.ExpandMore, null, tint = colors.textSecondary,
                modifier = Modifier.size(18.dp))
        }
        CompositionLocalProvider(LocalAppColors provides colors) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.surface,
            ) {
                ModeMenuItem(stringResource(R.string.chat), Icons.AutoMirrored.Filled.Chat,
                    current == ChatMode.CHAT || current == ChatMode.MAX) {
                    onSelect(ChatMode.CHAT); expanded = false
                }
                ModeMenuItem(stringResource(R.string.code), Icons.Default.Code, current == ChatMode.CODE) {
                    onSelect(ChatMode.CODE); expanded = false
                }
                ModeMenuItem(stringResource(R.string.t_134), Icons.Default.Movie, current == ChatMode.MEDIA) {
                    onSelect(ChatMode.MEDIA); expanded = false
                }
            }
        }
    }
}

@Composable
private fun ModeMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    DropdownMenuItem(
        modifier = Modifier.background(colors.surface),
        colors = MenuDefaults.itemColors(
            textColor = colors.textPrimary,
            leadingIconColor = colors.textPrimary,
        ),
        text = {
            Text(
                label,
                color = if (selected) colors.accent else colors.textPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(icon, null, tint = if (selected) colors.accent else colors.textSecondary)
        },
    )
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
                Text(stringResource(R.string.t_047), color = TextSecondary, fontSize = 15.sp)
                Text(stringResource(R.string.t_048), color = TextTertiary, fontSize = 13.sp,
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
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .combinedClickable(
                        onClick = { onOpen(s) },
                        onLongClick = { onLongPress(s) },
                    ),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
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
    // ⭐ محلي: صورة مرفوعة لم تُرفع للسيرفر بعد
    val localImage = msg.localImageUri

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = if (isUser) Modifier.widthIn(max = 320.dp) else Modifier.fillMaxWidth(),
        ) {
            if (isUser) {
                Surface(
                    color = BubbleBg,
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BubbleBorder),
                    shadowElevation = 4.dp,
                    modifier = Modifier.combinedClickable(
                        onClick = { },
                        onLongClick = onLongPress,
                    ),
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        // ⭐ صورة محلية (مرفوعة من الجهاز)
                        if (localImage != null && imageUrl == null) {
                            AsyncImage(
                                model = localImage,
                                contentDescription = stringResource(R.string.t_135),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .widthIn(max = 220.dp)
                                    .heightIn(max = 260.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                            if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                        }
                        // ⭐ صورة من السيرفر
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = stringResource(R.string.t_135),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .combinedClickable(
                                        onClick = { onImageClick(imageUrl) },
                                        onLongClick = onLongPress,
                                    ),
                            )
                            if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                        }
                        if (cleanText.isNotBlank()) {
                            Text(
                                cleanText,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 22.sp,
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { },
                            onLongClick = onLongPress,
                        ),
                ) {
                    // ⭐ مؤشر جاري التحليل
                    if (msg.isAnalyzing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BubbleBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Accent,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "🔍 جاري تحليل الصورة...",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = stringResource(R.string.t_135),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .clip(RoundedCornerShape(12.dp))
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
                            Text(stringResource(R.string.t_049), color = TextPrimary,
                                fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                    }
                    if (cleanText.isNotBlank()) {
                        MarkdownText(cleanText, textColor = TextPrimary, fontSize = 16)
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

                ActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = stringResource(R.string.t_003),
                    onClick = { onCopy(cleanText) },
                )

                if (isUser) {
                    ActionButton(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.t_063),
                        onClick = { onEdit(msg) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
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
    Surface(color = BgSecondary, shape = RoundedCornerShape(20.dp)) {
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
                Text(stringResource(R.string.t_050), color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(content.take(60), color = TextSecondary, fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, stringResource(R.string.t_026), tint = TextSecondary,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 📋 AllSessionsListView — Chat + Code مع أيقونة نوع
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AllSessionsListView(
    sessions: List<AllSession>,
    onOpenChat: (AllSession) -> Unit,
    onOpenCode: (AllSession) -> Unit,
    onLongPressChat: (AllSession) -> Unit,
) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.t_047), color = TextSecondary, fontSize = 15.sp)
                Text(stringResource(R.string.t_048), color = TextTertiary, fontSize = 13.sp,
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
                    .shadow(4.dp, RoundedCornerShape(20.dp))
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
                shape = RoundedCornerShape(20.dp),
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
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            s.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                        s.lastMsg?.let {
                            Text(
                                it.take(80),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper: AllSession → Session (للحذف)
private fun AllSession.toChatSession(): Session = Session(
    id = this.id,
    title = this.title,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    msgCount = this.msgCount,
    lastMsg = this.lastMsg,
)

private fun getFileSize(ctx: android.content.Context, uri: Uri): Long {
    return try {
        ctx.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
    } catch (_: Exception) { 0L }
}
