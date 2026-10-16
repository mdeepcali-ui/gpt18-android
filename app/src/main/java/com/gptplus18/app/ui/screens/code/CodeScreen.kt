@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.gptplus18.app.ui.screens.code

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.gptplus18.app.ui.components.AttachMenuSheet
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.gptplus18.app.ui.components.AppDrawerContent
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
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.ui.screens.chat.MessageBubble
import com.gptplus18.app.ui.components.ChatGptComposer
import com.gptplus18.app.ui.components.MarkdownText
import com.gptplus18.app.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.gptplus18.app.ui.screens.chat.components.RunButton
import com.gptplus18.app.ui.screens.chat.components.RunDialog
import com.gptplus18.app.util.ProjectSaver
import android.widget.Toast
import com.gptplus18.app.ui.components.icons.ServerIcon
import com.gptplus18.app.ui.components.icons.FolderIcon
import com.gptplus18.app.ui.screens.chat.components.SaveButton
import com.gptplus18.app.ui.screens.chat.components.SaveDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CodeScreen(
    openSessionId: Int? = null,
    onNavigateToChat: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToServers: () -> Unit = {},
    onNavigateToProjects: () -> Unit = {},
    vm: CodeViewModel = hiltViewModel(),
) {

    androidx.compose.runtime.LaunchedEffect(openSessionId) {
        if (openSessionId != null && openSessionId > 0) {
            vm.openSession(openSessionId)
        }
    }

    // ⭐ v18.8: فحص المهام الجارية عند فتح الشاشة
    LaunchedEffect(Unit) {
        vm.checkActiveTasks()
    }

    val clip = LocalClipboardManager.current
    val ctx = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // ⭐ v5.0: مرفقات — إضافة launchers (نفس ChatScreen)

    fun _sizeOf(uri: Uri): Long = try {
        ctx.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
    } catch (_: Exception) { 0L }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            ui.cameraUri?.let { uri ->
                vm.addAttachment(uri, "image/jpeg", "camera_${System.currentTimeMillis()}.jpg", _sizeOf(uri))
            }
        }
    }

    fun launchCamera() {
        try {
            val f = java.io.File(ctx.cacheDir, "cam_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                ctx, "${ctx.packageName}.fileprovider", f,
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
            vm.addAttachment(it, mime, "image_${System.currentTimeMillis()}.jpg", _sizeOf(it))
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val mime = ctx.contentResolver.getType(it) ?: "application/octet-stream"
            vm.addAttachment(it, mime, "file_${System.currentTimeMillis()}", _sizeOf(it))
        }
    }


    // X2: viewer
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    val ui = rememberCodeLocalState()

    // ⭐ v5.0.9: هل المستخدم مبتعد عن الأسفل؟
    val isScrolledUp by remember {
        androidx.compose.runtime.derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            if (total == 0) return@derivedStateOf false
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            val diff = total - 1 - last.index
            diff >= 3
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val haptics = com.gptplus18.app.ui.theme.rememberAppHaptics()

    // ⭐ v3.3: auto-scroll محسّن — يحل مشكلة "الرد مخفي فوق"
    LaunchedEffect(
        state.messages.size,
        state.messages.lastOrNull()?.content?.length,
        state.isRunning,
    ) {
        val total = state.messages.size + if (state.isRunning) 1 else 0
        if (total <= 0) return@LaunchedEffect
        kotlinx.coroutines.delay(80)
        repeat(3) { attempt ->
            val count = listState.layoutInfo.totalItemsCount
            val target = if (count > 0) count - 1 else total - 1
            try {
                listState.animateScrollToItem(target)
            } catch (_: Exception) {}
            if (attempt < 2) kotlinx.coroutines.delay(60)
        }
    }

    // 🆕 عند فتح الشاشة — افتح جلسة برمجة جديدة تلقائياً
    LaunchedEffect(Unit) {
        if (state.currentSessionId == null) {
            vm.newRequest()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            androidx.compose.runtime.LaunchedEffect(Unit) { vm.refreshAvatar() }
            AppDrawerContent(
                userName = state.userName,
                userEmail = "",
                isOwner = state.isOwner,
                onNewChat = {
                    vm.newRequest()
                    scope.launch { drawerState.close() }
                },
                onSessionsClick = {
                    vm.backToList()
                    scope.launch { drawerState.close() }
                },
                lastSessionTitle = null,
                onChat = {
                    scope.launch { drawerState.close() }
                    onNavigateToChat()
                },
                onCode = { scope.launch { drawerState.close() } },
                onMedia = { scope.launch { drawerState.close() } },
                onSubscription = {
                    scope.launch { drawerState.close() }
                    onNavigateToSubscription()
                },
                onProfile = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                },
                onSettings = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onProjects = {
                    scope.launch { drawerState.close() }
                    onNavigateToProjects()
                },
                onServers = {
                    scope.launch { drawerState.close() }
                    onNavigateToServers()
                },
                onAdmin = {
                    scope.launch { drawerState.close() }
                    onNavigateToAdmin()
                },
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
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CodeHeader(
                isRunning = state.isRunning,
                onDrawerOpen = { scope.launch { drawerState.open() } },
                onNavigateToChat = onNavigateToChat,
                onRunClick = { vm.clearRunState(); ui.showRunDialog = true },
                onSaveClick = { vm.showSaveDialog() },
                onNewRequest = { vm.newRequest() },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            // ⭐ v5.1.11: فقاعة الحالة — نفس موقعها في الشات
            androidx.compose.animation.AnimatedVisibility(
                visible = state.isRunning && state.currentSessionId != null,
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
                    .zIndex(100f)
                    .padding(top = 8.dp, start = 12.dp),
            ) {
                com.gptplus18.app.ui.screens.chat.StatusBubble(
                    label = state.statusLabel.ifBlank { "التفكير" }
                )
            }

            if (state.currentSessionId == null) {
                SessionList(
                    sessions = state.sessions,
                    onOpen = { vm.openSession(it.id) },
                    onLongPress = { haptics.openDialog(); ui.deleteDialogFor = it },
                )
            } else {
                Column(Modifier.fillMaxSize().imePadding()) {
                    // ⭐ v2.0: رسالة مثبتة للمستخدم المجاني
                    if (!state.hasCodeAccess && !state.isOwner) {
                        com.gptplus18.app.ui.components.CodeAccessBanner(
                            userName = state.userName,
                            credits = state.credits,
                            usdValue = state.usdValue,
                            onSubscribe = onNavigateToSubscription,
                        )
                    }

                    // ⭐ v9.3: مسافة ديناميكية — تختفي عند فتح الكيبورد
                    val _imeVisible = WindowInsets.isImeVisible
                    val _bottomPad = if (_imeVisible) 8.dp else 40.dp
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val _down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
                                if (_down.isConsumed) return@awaitEachGesture
                                val _t0 = System.currentTimeMillis()
                                val _up = waitForUpOrCancellation(pass = PointerEventPass.Final)
                                if (_up != null && !_up.isConsumed) {
                                    val _dur = System.currentTimeMillis() - _t0
                                    val _moved = (_up.position - _down.position).getDistance() > viewConfiguration.touchSlop
                                    if (_dur < 400 && !_moved) { ui.selectionEpoch++ }
                                }
                            }
                        },
                    ) {
                    CodeMessagesList(
                        state = state,
                        listState = listState,
                        scope = scope,
                        haptics = haptics,
                        clip = clip,
                        selectionEpoch = ui.selectionEpoch,
                        bottomPad = _bottomPad,
                        isScrolledUp = isScrolledUp,
                        onFullscreenImage = { url -> ui.fullscreenImage = url },
                        onEditInput = { text -> ui.input = text },
                    )
                    }  // نهاية Box


                    CodeComposer(
                        value = ui.input,
                        onValueChange = { ui.input = it },
                        state = state,
                        vm = vm,
                        keyboardController = keyboardController,
                        haptics = haptics,
                        onAttachRequest = { ui.showAttachSheet = true },
                    )
                }
            }
        }
    }

    // ⭐ v19.0: Overlays — CodeOverlays
    CodeOverlays(
        state = state,
        vm = vm,
        haptics = haptics,
        showAttachSheet = ui.showAttachSheet,
        deleteDialogFor = ui.deleteDialogFor,
        fullscreenImage = ui.fullscreenImage,
        showRunDialog = ui.showRunDialog,
        onDismissAttach = { ui.showAttachSheet = false },
        onDismissDelete = { ui.deleteDialogFor = null },
        onDismissFullscreen = { ui.fullscreenImage = null },
        onDismissRun = { ui.showRunDialog = false; vm.clearRunState() },
        onCamera = { launchCamera() },
        onPhotos = { imagePicker.launch("image/*") },
        onFiles = { filePicker.launch("*/*") },
        onEditInput = { text -> ui.input = text },
    )

    // ⭐ v10.0: save_to_phone handler — المساعد يطلب حفظ مشروع
    LaunchedEffect(state.saveRequest) {
        val req = state.saveRequest ?: return@LaunchedEffect
        vm.markSaveInProgress()
        val result = ProjectSaver.saveProject(
            context = ctx,
            folderName = req.folderName,
            files = req.files,
            binaries = req.binaries,
            totalBytes = req.totalBytes,
        )
        if (result.success) {
            vm.onSaveSuccess(result.savedCount, result.folderPath)
            Toast.makeText(
                ctx,
                "✅ ${result.savedCount} ملف → ${result.folderPath}",
                Toast.LENGTH_LONG,
            ).show()
        } else {
            vm.onSaveError(result.error)
            Toast.makeText(
                ctx,
                "❌ فشل الحفظ: ${result.error}",
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
