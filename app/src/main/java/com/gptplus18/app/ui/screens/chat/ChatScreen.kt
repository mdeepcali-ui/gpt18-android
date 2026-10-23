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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
import com.gptplus18.app.ui.components.VideoPlayerCard
import com.gptplus18.app.ui.components.AudioPlayerCard
import com.gptplus18.app.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.pointerInput
import com.gptplus18.app.ui.components.ReplyBar
import com.gptplus18.app.ui.components.StatusBubble


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToCode: (Int) -> Unit = {},
    vm: ChatViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = com.gptplus18.app.ui.theme.rememberAppHaptics()
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()
    val ui = rememberChatLocalState()
    val clip = LocalClipboardManager.current

    // 📋 قائمة المحادثات — تُفتح من Drawer فقط

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            ui.cameraUri?.let { uri ->
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
            ui.cameraUri = uri
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
    // 📨 تتبع عدد الرسائل لكشف "رسالة جديدة" مقابل streaming
    LaunchedEffect(imeHeight) {
        ui.lastImeChangeAt = System.currentTimeMillis()
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
        val sizeIncreased = state.messages.size > ui.prevMsgSize
        ui.prevMsgSize = state.messages.size

        // إذا فقط تغيّر المحتوى (streaming) + الكيبورد تحرك تواً → تجاهل
        if (!sizeIncreased) {
            val msSinceImeChange = System.currentTimeMillis() - ui.lastImeChangeAt
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
    BackHandler(enabled = ui.showSessionsList) {
        ui.showSessionsList = false
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
            // ⭐ v5.1.34: إعادة تحميل الصورة كل مرة تُفتح القائمة
            androidx.compose.runtime.LaunchedEffect(Unit) { vm.refreshAvatar() }
            AppDrawerContent(
                userName = state.userName,
                userEmail = "",
                isOwner = state.isOwner,
                onNewChat = {
                    vm.newChat()
                    ui.showSessionsList = false
                    scope.launch { drawerState.close() }
                },
                onSessionsClick = {
                    ui.showSessionsList = true
                    vm.loadAllSessions()   // ⭐ v5.0.13: إعادة تحميل عند الفتح
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
                // ⭐ v2.0: الرصيد والاشتراك
                credits = state.credits,
                usdValue = state.usdValue,
                subscriptionPlan = state.subscriptionPlan,
            )
        },
    ) {
        Scaffold(
            containerColor = BgPrimary,
            topBar = {
                ChatHeader(
                    state = state,
                    showSearch = ui.showSearch,
                    searchQuery = ui.searchQuery,
                    showSessionsList = ui.showSessionsList,
                    onSearchChange = { ui.searchQuery = it; vm.setSearch(it) },
                    onSearchOpen = { ui.showSearch = true },
                    onSearchClose = { ui.showSearch = false; ui.searchQuery = ""; vm.setSearch("") },
                    onModeSelect = { mode -> vm.setMode(mode) },
                    onDrawerOpen = { scope.launch { drawerState.open() } },
                    onNewChat = { vm.newChat() },
                    onNavigateToCode = onNavigateToCode,
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // ⭐ فقاعة الحالة (يفكر / يحلل) — أعلى اليمين في RTL
                androidx.compose.animation.AnimatedVisibility(
                    visible = (state.isSending || state.isUploading) && !ui.showSessionsList,
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
                        .zIndex(100f)                   // ⭐ فوق الرسائل والصور
                        .padding(top = 8.dp, start = 12.dp),
                ) {
                    StatusBubble(label = state.statusLabel.ifBlank { "التفكير" })
                }

                if (ui.showSessionsList) {
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
                                ui.showSessionsList = false
                            },
                            onOpenCode = { session ->
                                ui.showSessionsList = false
                                onNavigateToCode(session.id)
                            },
                            onLongPressChat = { ui.deleteDialogFor = it.toChatSession() },
                        )
                    }
                } else {
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        // ⭐ v9.3: مسافة ديناميكية — تختفي عند فتح الكيبورد
                        val _imeVisible = WindowInsets.isImeVisible
                        val _bottomPad = if (_imeVisible) 8.dp else 40.dp
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        val _down = awaitFirstDown(
                                            requireUnconsumed = false,
                                            pass = PointerEventPass.Final
                                        )
                                        if (_down.isConsumed) return@awaitEachGesture
                                        val _t0 = System.currentTimeMillis()
                                        val _up = waitForUpOrCancellation(
                                            pass = PointerEventPass.Final
                                        )
                                        if (_up != null && !_up.isConsumed) {
                                            val _dur = System.currentTimeMillis() - _t0
                                            val _moved = (_up.position - _down.position).getDistance() > viewConfiguration.touchSlop
                                            if (_dur < 400 && !_moved) {
                                                ui.selectionEpoch++
                                            }
                                        }
                                    }
                                },
                        ) {
                        ChatMessagesList(
                            state = state,
                            vm = vm,
                            listState = listState,
                            scope = scope,
                            haptics = haptics,
                            ctx = ctx,
                            clip = clip,
                            selectionEpoch = ui.selectionEpoch,
                            bottomPad = _bottomPad,
                            showSessionsList = ui.showSessionsList,
                            isScrolledUp = isScrolledUp,
                            onFullscreenImage = { url -> ui.fullscreenImage = url },
                            onLongPress = { msg -> ui.actionsSheetFor = msg },
                            onEditInput = { text -> ui.input = text },
                        )
                        }  // نهاية Box

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
                        if (ui.input.isBlank() && state.pendingAttachments.isEmpty()) {

                        }
                        // ⭐ السحابة: عنوان "التفكير" + مربع أسود مع الرد الجاري


                        ChatComposer(
                            value = ui.input,
                            onValueChange = { ui.input = it },
                            state = state,
                            vm = vm,
                            keyboardController = keyboardController,
                            haptics = haptics,
                            onAttachRequest = { ui.showAttachSheet = true },
                            onImagePickRequest = { ui.showAttachSheet = true },
                            onEditImagePickRequest = { ui.showAttachSheet = true },
                        )
                    }

                    // (FAB نُقل داخل Box v5.0.10)
                    }
                }
            }
        }
    }

    ChatOverlays(
        state = state,
        vm = vm,
        haptics = haptics,
        ctx = ctx,
        clip = clip,
        showAttachSheet = ui.showAttachSheet,
        showAddonsSheet = ui.showAddonsSheet,
        showDeepThinkSheet = ui.showDeepThinkSheet,
        deleteDialogFor = ui.deleteDialogFor,
        actionsSheetFor = ui.actionsSheetFor,
        fullscreenImage = ui.fullscreenImage,
        input = ui.input,
        onInputChange = { ui.input = it },
        onDismissAttach = { ui.showAttachSheet = false },
        onDismissAddons = { ui.showAddonsSheet = false },
        onDismissDeepThink = { ui.showDeepThinkSheet = false },
        onDismissDelete = { ui.deleteDialogFor = null },
        onDismissActions = { ui.actionsSheetFor = null },
        onDismissFullscreen = { ui.fullscreenImage = null },
        onOpenAddons = { ui.showAddonsSheet = true },
        onOpenDeepThink = { ui.showDeepThinkSheet = true },
        onCamera = { launchCamera() },
        onPhotos = { imagePicker.launch("image/*") },
        onFiles = { filePicker.launch("*/*") },
        onNavigateToSubscription = onNavigateToSubscription,
    )
}
