package com.gptplus18.app.ui.screens.chat
import androidx.compose.animation.core.animateFloat

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

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
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

    // 🎯 هل المستخدم مبتعد عن الأسفل بمسافة كبيرة؟
    // يعني الـ auto-scroll يشتغل إلا إذا المستخدم صعد كتير فوق
    val isScrolledUp by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            if (total == 0) return@derivedStateOf false
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            // نحسب الفرق بين العنصر الأخير المرئي والأخير الفعلي
            val diff = total - 1 - last.index
            // نعتبره "scrolled up" فقط إذا ابتعد 3 عناصر أو أكثر
            diff >= 3
        }
    }


    // ⭐ تتبّع ارتفاع الكيبورد — لتجنب scroll غير مرغوب
    val density = androidx.compose.ui.platform.LocalDensity.current
    val imeHeight = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(density)

    // ⏱ آخر وقت تغيّر فيه الكيبورد (لكشف فتح/غلق تو)
    var lastImeChangeAt by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    // 📨 تتبع عدد الرسائل لكشف "رسالة جديدة" مقابل streaming
    var prevMsgSize by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    LaunchedEffect(imeHeight) {
        lastImeChangeAt = System.currentTimeMillis()
    }

    // 🎯 auto-scroll — يشتغل فقط:
    //   1) عند إضافة رسالة جديدة
    //   2) أو أثناء streaming والمستخدم عند الأسفل
    //   3) وليس بسبب فتح/إغلاق الكيبورد (نتجاهل 350ms بعد تغيّر الـ ime)
    LaunchedEffect(
        state.messages.size,
        state.messages.lastOrNull()?.content?.length,
    ) {
        // ⭐ هل زادت الرسائل؟ (رسالة جديدة أُرسلت)
        val sizeIncreased = state.messages.size > prevMsgSize
        prevMsgSize = state.messages.size

        // إذا فقط تغيّر المحتوى (streaming) + الكيبورد تحرك تواً → تجاهل
        if (!sizeIncreased) {
            val msSinceImeChange = System.currentTimeMillis() - lastImeChangeAt
            if (msSinceImeChange < 350) {
                return@LaunchedEffect
            }
        }

        // ⭐ إذا رسالة جديدة → انزل دائماً. إذا streaming → فقط لو المستخدم عند الأسفل
        val shouldScroll = sizeIncreased || !isScrolledUp
        if (!shouldScroll) return@LaunchedEffect

        // ننتظر شوي حتى تُضاف العناصر الجديدة (رسالة المستخدم + يفكر)
        kotlinx.coroutines.delay(if (sizeIncreased) 120 else 80)

        // نكرر المحاولة لضمان الوصول للنهاية (العناصر قد تُضاف على دفعات)
        repeat(3) { attempt ->
            val count = listState.layoutInfo.totalItemsCount
            if (count > 0) {
                try {
                    listState.animateScrollToItem(count - 1)
                } catch (_: Exception) {}
            }
            if (attempt < 2) kotlinx.coroutines.delay(50)
        }
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
                onCode = { scope.launch { drawerState.close() }; vm.setMode(ChatMode.CODE) },
                onMedia = { scope.launch { drawerState.close() }; vm.setMode(ChatMode.MEDIA) },
                onSubscription = { scope.launch { drawerState.close() }; onNavigateToSubscription() },
                onProfile = { scope.launch { drawerState.close() }; onNavigateToProfile() },
                onSettings = { scope.launch { drawerState.close() }; onNavigateToSettings() },
                onAdmin = { scope.launch { drawerState.close() }; onNavigateToAdmin() },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onNavigateToAuth()
                },
                avatarUrl = state.avatarUrl,
            )
        },
    ) {
        Scaffold(
            containerColor = BgPrimary,
            topBar = {
              Column(Modifier.fillMaxWidth().background(BgPrimary).statusBarsPadding()) {
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
                        modifier = Modifier.height(40.dp),
                        windowInsets = WindowInsets(0, 0, 0, 0),
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
                                        onSelect = { mode -> vm.setMode(mode) },
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
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp, top = 2.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(3.0.dp, TextPrimary, CircleShape)
                                    .clickable { vm.newChat() },
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
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(Icons.Default.Search, stringResource(R.string.t_128), tint = TextPrimary)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgPrimary),
                        modifier = Modifier.height(40.dp),
                        windowInsets = WindowInsets(0, 0, 0, 0),
                    )
                }
              }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // ⭐ فقاعة الحالة (يفكر / يحلل) — أعلى اليمين في RTL
                androidx.compose.animation.AnimatedVisibility(
                    visible = (state.isSending || state.isUploading) && !showSessionsList,
                    enter = androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = androidx.compose.animation.core.tween(260)
                    ) + androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(220)
                    ),
                    exit = androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = androidx.compose.animation.core.tween(200)
                    ) + androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(180)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 8.dp, start = 12.dp),
                ) {
                    StatusBubble(label = state.statusLabel)
                }

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
                                vm.setMode(ChatMode.CODE)
                                showSessionsList = false
                            },
                            onLongPressChat = { deleteDialogFor = it.toChatSession() },
                        )
                    }
                } else {
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            val visibleMessages = state.messages.filter { it.role != "thinking" }
                            // EMPTY_LOGO_STATE
                            if (visibleMessages.isEmpty() && !state.isSending && !state.isUploading) {
                                item {
                                    EmptyLogoState()
                                }
                            }
                            items(visibleMessages, key = { it.ts.toString() }) { msg ->
                                MessageBubble(
                                    msg = msg,
                                    onImageClick = { url -> fullscreenImage = url },
                                    onLongPress = { actionsSheetFor = msg },
                                    onCopy = { text -> clip.setText(AnnotatedString(text)) },
                                    onEdit = { m -> input = m.content },
                                    onChoiceClick = { choice ->
                                        // ⭐ v2.0: إرسال الاختيار مباشرة
                                        vm.send(choice)
                                    },
                                )
                            }
                            if (state.isSending || state.isUploading) {
                                item {
                                    // ☁️ سحابة التفكير فقط
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
                                    // ⭐ v2.0: Skeleton loader للوسائط قيد الإنشاء
                                    if (state.pendingMediaType != null) {
                                        com.gptplus18.app.ui.components.MediaSkeletonLoader(
                                            type = state.pendingMediaType!!,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    if (state.statusLabel != stringResource(R.string.t_131)) {
                                        com.gptplus18.app.ui.components.ThinkingShimmer(
                                            text = thinkingText.ifBlank { "يجهّز الرد..." },
                                        )
                                    }
                                }
                            }

                            // ⭐ v2.0: بطاقة ملفات الكود
                            if (state.codeFiles.isNotEmpty() || state.codeZipUrl != null) {
                                item {
                                    CodeFilesCard(
                                        files = state.codeFiles,
                                        zipUrl = state.codeZipUrl,
                                    )
                                }
                            }
                            }

                        if (state.pinnedMessages.isNotEmpty()) {
                            PinnedMessagesBar(
                                messages = state.pinnedMessages,
                                onUnpin = { ts -> vm.unpinMessage(ts) },
                            )
                        }

                        state.replyTo?.let { r ->
                            ReplyBar(content = r.content, onCancel = { vm.setReplyTo(null) })
                        }

                        // QUICK_ACTIONS_MARKER
                        if (input.isBlank() && state.pendingAttachments.isEmpty()) {

                        }
                        ChatGptComposer(
                            value = input,
                            onValueChange = { input = it },
                            enabled = !state.isSending && !state.isUploading,
                            attachments = state.pendingAttachments,
                            onAttachClick = { showAttachSheet = true },
                            onSend = {
                                if (state.pendingAttachments.isNotEmpty()) {
                                    vm.sendWithAttachments(input)
                                } else if (input.isNotBlank()) {
                                    vm.send(input)
                                }
                                input = ""
                                // ⭐ إغلاق الكيبورد تلقائياً
                                keyboardController?.hide()
                            },
                            onRemoveAttachment = { vm.removeAttachment(it) },
                            onVoiceInput = { text -> input = if (input.isBlank()) text else "$input $text" },
                            onImagePick = { showAttachSheet = true },       // ⭐ v2.0: فتح bottom sheet لاختيار صورة
                            onEditImagePick = { showAttachSheet = true },   // ⭐ v2.0: نفس الـ sheet
                        )
                    }

                    // ⬇ FAB للنزول السريع
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isScrolledUp,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 96.dp),
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(BgSecondary)
                                .border(1.5.dp, TextSecondary.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                                .clickable {
                                    scope.launch {
                                        val total = listState.layoutInfo.totalItemsCount
                                        if (total > 0) {
                                            listState.animateScrollToItem(total - 1)
                                        }
                                    }
                                },
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                contentDescription = "انزل للأسفل",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
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
            isPinned = vm.isPinned(msg.ts),
            onPin = { vm.pinMessage(msg) },
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
    // ⭐ v2.0: كل شي في Chat — عنوان فقط بدون قائمة
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Chat", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
private fun PinnedMessagesBar(
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
                    Text("\ud83d\udccc", fontSize = 11.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "\u0645\u062b\u0628\u0651\u062a",
                        color = Accent,
                        fontSize = 10.sp,
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
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
            IconButton(
                onClick = { onUnpin(latest.ts) },
                modifier = Modifier.size(24.dp),
            ) {
                Text("\u2715", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

// ⭐ v2.0: بطاقة ملفات الكود الأنيقة
@Composable
private fun CodeFilesCard(
    files: List<com.gptplus18.app.data.api.CodeFileItem>,
    zipUrl: String?,
) {
    val ctx = LocalContext.current
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1F1F26), Color(0xFF141419))
                )
            )
            .border(
                width = 0.6.dp,
                color = Accent.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(14.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📁", fontSize = 17.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الملفات جاهزة",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${files.size} ملف${if (zipUrl != null) " — أو حمّل الكل" else ""}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // قائمة الملفات
            files.forEach { f ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.4f))
                        .clickable {
                            try {
                                val i = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(f.url))
                                ctx.startActivity(i)
                            } catch (_: Exception) {}
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text("📄", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = f.name,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Text(
                        text = "⬇",
                        color = Accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // زر ZIP
            if (zipUrl != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Accent, Accent.copy(alpha = 0.8f))
                            )
                        )
                        .clickable {
                            try {
                                val i = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(zipUrl))
                                ctx.startActivity(i)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 11.dp),
                ) {
                    Text("📦", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "تحميل الكل (ZIP)",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ⭐ v2.0: QuickActionsRow removed — التوجيه الآن عبر StatusBubble فقط

@Composable
private fun EmptyLogoState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_transparent),
            contentDescription = "GPT+18",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier.size(180.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "\u0645\u0631\u062d\u0628\u0627 \u0628\u0643 \u0641\u064a GPT+18",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun _EmptyChatStateUnused(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "\u0627\u0634\u0631\u062d \u0644\u064a \u0627\u0644\u0630\u0643\u0627\u0621 \u0627\u0644\u0627\u0635\u0637\u0646\u0627\u0639\u064a \ud83d\udca1",
        "\u0627\u0643\u062a\u0628 \u0644\u064a \u062f\u0627\u0644\u0629 Python \ud83d\udcbb",
        "\u0623\u062e\u0628\u0627\u0631 \u0627\u0644\u062a\u0642\u0646\u064a\u0629 \u0627\u0644\u064a\u0648\u0645 \ud83d\udcf0",
        "\u0627\u0635\u0646\u0639 \u0644\u064a \u0635\u0648\u0631\u0629 \u0642\u0637\u0629 \ud83c\udfa8",
        "\u0644\u062e\u0651\u0635 \u0644\u064a \u0645\u0642\u0627\u0644\u0629 \ud83d\udcdd",
        "\u0627\u0642\u062a\u0631\u062d \u0639\u0644\u064a\u0651 \u0641\u0643\u0631\u0629 \u0645\u0634\u0631\u0648\u0639 \ud83e\udd14",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Accent.copy(alpha = 0.08f))
                .border(1.5.dp, Accent.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("\u2728", fontSize = 42.sp)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "\u0645\u0631\u062d\u0628\u0627 \u0628\u0643 \u0641\u064a GPT+18",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "\u0627\u0628\u062f\u0623 \u0628\u0627\u0644\u0643\u062a\u0627\u0628\u0629 \u0641\u064a \u0627\u0644\u0623\u0633\u0641\u0644 \u0623\u0648 \u0627\u062e\u062a\u0631 \u0627\u0642\u062a\u0631\u0627\u062d\u0627\u064b:",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(22.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { s ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0E0E12))
                        .border(1.dp, Color(0xFF1F1F26), RoundedCornerShape(12.dp))
                        .clickable {
                            // نشيل الإيموجي من النهاية
                            onSuggestionClick(s.substringBeforeLast(" "))
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(s, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    msg: Message,
    onImageClick: (String) -> Unit,
    onLongPress: () -> Unit,
    onCopy: (String) -> Unit,
    onEdit: (Message) -> Unit,
    onChoiceClick: (String) -> Unit = {},
) {
    val isUser = msg.role == "user"
    val imageUrl = MessageHelpers.extractImageUrl(msg.content)
    val audioUrl = MessageHelpers.extractAudioUrl(msg.content)
    val videoUrl = MessageHelpers.extractVideoUrl(msg.content)
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
                            // ✨ streaming: فضي ناعم — بعد الاكتمال: أبيض عادي
                            val isStreaming = (msg.id == -2)
                            Text(
                                cleanText,
                                color = if (isStreaming) androidx.compose.ui.graphics.Color(0xFFC8C8CC) else TextPrimary,
                                fontSize = if (isStreaming) 13.sp else 16.sp,
                                fontWeight = if (isStreaming) FontWeight.Normal else FontWeight.SemiBold,
                                lineHeight = if (isStreaming) 19.sp else 22.sp,
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
                        com.gptplus18.app.ui.components.AudioPlayerCard(
                            audioUrl = audioUrl,
                            title = "أغنية",
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                    }
                    if (videoUrl != null) {
                        com.gptplus18.app.ui.components.VideoPlayerCard(
                            videoUrl = videoUrl,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (cleanText.isNotBlank()) Spacer(Modifier.height(8.dp))
                    }
                    if (cleanText.isNotBlank()) {
                        com.gptplus18.app.ui.components.MarkdownTextBox(cleanText, textColor = TextPrimary, fontSize = 15)
                    }
                }
            }

            // ⭐ v2.0: أزرار الاختيار (حللها / عدّلها / فيديو)
            val choices = MessageHelpers.extractChoices(msg.content)
            if (choices.isNotEmpty() && !isUser) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(choices) { ch ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(Accent.copy(alpha = 0.12f))
                                .border(0.8.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .clickable { onChoiceClick(ch) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(ch, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                } else {
                    // 👍 / 👎 / 🔗 لردود AI
                    var reaction by remember { mutableStateOf(0) }
                    val shareCtx = LocalContext.current

                    ReactionButton(
                        symbol = "\uD83D\uDC4D",
                        active = reaction == 1,
                        onClick = { reaction = if (reaction == 1) 0 else 1 },
                    )
                    ReactionButton(
                        symbol = "\uD83D\uDC4E",
                        active = reaction == -1,
                        onClick = { reaction = if (reaction == -1) 0 else -1 },
                    )
                    ReactionButton(
                        symbol = "\uD83D\uDD17",
                        active = false,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, cleanText)
                                }
                                shareCtx.startActivity(Intent.createChooser(intent, null))
                            } catch (_: Exception) {}
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReactionButton(
    symbol: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(
                if (active) Accent.copy(alpha = 0.25f)
                else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = 1.dp,
                color = if (active) Accent.copy(alpha = 0.6f)
                        else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            fontSize = 14.sp,
            color = if (active) Accent else TextSecondary,
        )
    }
}

@Composable
fun ActionButton(
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

@OptIn(ExperimentalFoundationApi::class)
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
                        modifier = Modifier.size(28.dp),
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


/**
 * ⭐ فقاعة الحالة (يفكر / يحلل / يرفع) — تحت البار العلوي
 * - موقع: أعلى اليمين (TopStart في RTL)
 * - شعاع لامع يمر من اليسار لليمين كل 0.7s
 * - نقطة زرقاء نابضة
 */
@Composable
fun StatusBubble(label: String) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")

    // 🌟 حركة الشعاع
    val shimmerX by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 700,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )

    // 🔵 نبضة النقطة
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1400),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1400),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    // 🎨 ألوان متكيفة — أسود/أبيض مع شفافية خفيفة (تمنع ظهور النص تحتها)
    val bgTop = if (isDark) Color(0xFF1C1C22).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.94f)
    val bgBot = if (isDark) Color(0xFF15151A).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f)
    val borderC = Accent.copy(alpha = if (isDark) 0.35f else 0.5f)
    val txtC = if (isDark) Color(0xFFE8E8EC) else Color(0xFF1A1A1A)
    val shadowC = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f)
    val shimmerHi = if (isDark) 0.18f else 0.35f

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .shadow(
                elevation = if (isDark) 6.dp else 3.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = shadowC,
                spotColor = shadowC,
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(colors = listOf(bgTop, bgBot))
            )
            .border(
                width = 0.5.dp,
                color = borderC,
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        // 🌟 طبقة الشعاع (تتحرك)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.White.copy(alpha = shimmerHi * 0.4f),
                            Color.White.copy(alpha = shimmerHi),
                            Color.White.copy(alpha = shimmerHi * 0.4f),
                            Color.Transparent,
                            Color.Transparent,
                        ),
                        startX = shimmerX * 400f,
                        endX = shimmerX * 400f + 200f,
                    )
                ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .widthIn(min = 160.dp)
                .padding(horizontal = 22.dp, vertical = 9.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size((7 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Accent.copy(alpha = pulseAlpha),
                                Accent.copy(alpha = pulseAlpha * 0.4f),
                            )
                        )
                    ),
            )
            Text(
                text = label,
                color = txtC,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp,
            )
        }
    }
}
