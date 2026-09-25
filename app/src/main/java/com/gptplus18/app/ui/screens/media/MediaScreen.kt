package com.gptplus18.app.ui.screens.media

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.R
import com.gptplus18.app.data.models.MediaHistoryItem
import com.gptplus18.app.ui.theme.*
import kotlinx.coroutines.launch

private val GradientBlueStart = Color(0xFF4A9EFF)
private val GradientBlueEnd   = Color(0xFF2C5FE0)
private val AccentGradient = Brush.linearGradient(
    colors = listOf(GradientBlueStart, GradientBlueEnd)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(vm: MediaViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var notifyMessage by remember { mutableStateOf<String?>(null) }
    var notifySuccess by remember { mutableStateOf(true) }
    LaunchedEffect(notifyMessage) {
        if (notifyMessage != null) {
            kotlinx.coroutines.delay(2500)
            notifyMessage = null
        }
    }

    var pendingSave by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun neededStoragePerms(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasAllStoragePerms(): Boolean {
        return neededStoragePerms().all {
            ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    val storagePermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val allGranted = result.values.all { it }
        val pending = pendingSave
        pendingSave = null
        if (pending != null && allGranted) {
            com.gptplus18.app.util.MediaShareHelper.saveToGallery(ctx, pending.first, pending.second) { ok ->
                notifySuccess = ok
                notifyMessage = if (ok) "\u2705 \u062a\u0645 \u0627\u0644\u062d\u0641\u0638 \u0628\u0646\u062c\u0627\u062d" else "\u274c \u0641\u0634\u0644 \u0627\u0644\u062d\u0641\u0638"
            }
        } else if (pending != null && !allGranted) {
            android.widget.Toast.makeText(ctx, "\u274c \u0646\u062d\u062a\u0627\u062c \u0625\u0630\u0646 \u0627\u0644\u062a\u062e\u0632\u064a\u0646", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun saveWithPermission(url: String, type: String) {
        val onResult: (Boolean) -> Unit = { ok ->
            notifySuccess = ok
            notifyMessage = if (ok) "\u2705 \u062a\u0645 \u0627\u0644\u062d\u0641\u0638 \u0628\u0646\u062c\u0627\u062d" else "\u274c \u0641\u0634\u0644 \u0627\u0644\u062d\u0641\u0638"
        }
        if (hasAllStoragePerms()) {
            com.gptplus18.app.util.MediaShareHelper.saveToGallery(ctx, url, type, onResult)
        } else {
            pendingSave = url to type
            storagePermLauncher.launch(neededStoragePerms())
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("\u0625\u0646\u0634\u0627\u0621 \u0627\u0644\u0648\u0633\u0627\u0626\u0637", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            if (state.tab == MediaTab.SONG) "\ud83c\udfb5 \u0623\u063a\u0627\u0646\u064a \u0628\u0627\u0644\u0630\u0643\u0627\u0621 \u0627\u0644\u0627\u0635\u0637\u0646\u0627\u0639\u064a"
                            else "\ud83c\udfac \u0641\u064a\u062f\u064a\u0648\u0647\u0627\u062a \u0628\u0627\u0644\u0630\u0643\u0627\u0621 \u0627\u0644\u0627\u0635\u0637\u0646\u0627\u0639\u064a",
                            color = TextSecondary, fontSize = 11.sp,
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Accent.copy(alpha = 0.15f))
                            .border(1.5.dp, Accent.copy(alpha = 0.4f), CircleShape)
                            .clickable { vm.setTab(MediaTab.SONG); vm.setPrompt("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Add, stringResource(R.string.new_chat), tint = Accent, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF14141A)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SegmentedTab("\ud83c\udfb5", "\u0623\u063a\u0646\u064a\u0629", state.tab == MediaTab.SONG, Modifier.weight(1f)) { vm.setTab(MediaTab.SONG) }
                SegmentedTab("\ud83c\udfac", "\u0641\u064a\u062f\u064a\u0648", state.tab == MediaTab.VIDEO, Modifier.weight(1f)) { vm.setTab(MediaTab.VIDEO) }
            }

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF0E0E12))
                    .border(1.dp, if (state.prompt.isNotBlank()) Accent.copy(alpha = 0.4f) else Color(0xFF1F1F26), RoundedCornerShape(16.dp)),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (state.tab == MediaTab.SONG) "\ud83c\udfb5" else "\ud83c\udfac", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (state.tab == MediaTab.SONG) "\u0648\u0635\u0641 \u0627\u0644\u0623\u063a\u0646\u064a\u0629" else "\u0648\u0635\u0641 \u0627\u0644\u0641\u064a\u062f\u064a\u0648",
                            color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.prompt,
                        onValueChange = { vm.setPrompt(it) },
                        placeholder = {
                            Text(
                                if (state.tab == MediaTab.SONG) "\u0645\u062b\u0627\u0644: \u0623\u063a\u0646\u064a\u0629 \u0631\u0648\u0645\u0627\u0646\u0633\u064a\u0629..."
                                else "\u0645\u062b\u0627\u0644: \u0645\u0634\u0647\u062f \u0637\u0628\u064a\u0639\u0629...",
                                color = TextTertiary, fontSize = 13.sp,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                            cursorColor = Accent,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (state.tab) {
                MediaTab.SONG -> {
                    SettingsSection("\u23f1\ufe0f \u0627\u0644\u0645\u062f\u0629", "\u23f1\ufe0f") {
                        listOf(30, 60, 120, 240).forEach { d -> NiceChip("$d \u062b", state.duration == d) { vm.setDuration(d) } }
                    }
                }
                MediaTab.VIDEO -> {
                    SettingsSection("\u0627\u0644\u0645\u062f\u0629", "\u23f1\ufe0f") {
                        listOf(5, 10, 15, 30).forEach { d -> NiceChip("$d \u062b", state.videoDuration == d) { vm.setVideoDuration(d) } }
                    }
                    Spacer(Modifier.height(12.dp))
                    SettingsSection("\u0627\u0644\u0646\u0645\u0648\u0630\u062c", "\ud83e\udd16") {
                        listOf("auto" to "\u062a\u0644\u0642\u0627\u0626\u064a", "ltx-2.3-spicy" to "LTX", "wan-2.2-spicy" to "Wan", "grok-imagine" to "Grok").forEach { (k, label) ->
                            NiceChip(label, state.videoModel == k) { vm.setVideoModel(k) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (state.isLoading) Color(0xFF1A1A22) else AccentGradient)
                    .clickable(enabled = !state.isLoading) { vm.generate() },
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(if (state.tab == MediaTab.VIDEO) "\u062c\u0627\u0631\u064a \u062a\u0648\u0644\u064a\u062f \u0627\u0644\u0641\u064a\u062f\u064a\u0648..." else "\u062c\u0627\u0631\u064a \u062a\u0648\u0644\u064a\u062f \u0627\u0644\u0623\u063a\u0646\u064a\u0629...", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (state.tab == MediaTab.SONG) "\ud83c\udfb5" else "\ud83c\udfac", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("\u0625\u0646\u0634\u0627\u0621", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            state.error?.let { err ->
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A1518))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(12.dp)).padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\u26a0\ufe0f", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(err, color = Color(0xFFFF7A7A), fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            if (state.tab == MediaTab.SONG && state.songUrl != null) {
                Spacer(Modifier.height(20.dp))
                ResultCardNew("\ud83c\udfb5", state.songTitle ?: "\u0623\u063a\u0646\u064a\u062a\u0643 \u062c\u0627\u0647\u0632\u0629", "\u0627\u0633\u0645\u0639\u060c \u062d\u0645\u0651\u0644\u060c \u0623\u0648 \u0634\u0627\u0631\u0643", state.songUrl!!, "song",
                    onSave = { u, t -> saveWithPermission(u, t) }, onRegenerate = { vm.regenerate() }, lyrics = state.songLyrics)
            }

            if (state.tab == MediaTab.VIDEO && state.videoUrl != null) {
                Spacer(Modifier.height(20.dp))
                ResultCardNew("\ud83c\udfac", "\u0627\u0644\u0641\u064a\u062f\u064a\u0648 \u062c\u0627\u0647\u0632",
                    state.videoModelUsed?.let { "\u0627\u0644\u0646\u0645\u0648\u0630\u062c: $it" } ?: "", state.videoUrl!!, "video",
                    onSave = { u, t -> saveWithPermission(u, t) }, onRegenerate = { vm.regenerate() }, lyrics = null)
            }

            Spacer(Modifier.height(28.dp))

            if (state.historyLoading) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("\u062a\u062d\u0645\u064a\u0644 \u0627\u0644\u0633\u062c\u0644...", color = TextSecondary, fontSize = 13.sp)
                }
            } else if (state.history.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("\ud83d\udcdc \u0623\u0639\u0645\u0627\u0644\u064a \u0627\u0644\u0623\u062e\u064a\u0631\u0629", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${state.history.size} \u0639\u0645\u0644 \u0645\u062d\u0641\u0648\u0638", color = TextTertiary, fontSize = 11.sp)
                    }
                    TextButton(onClick = { vm.clearHistory() }) { Text("\u0645\u0633\u062d", color = TextTertiary, fontSize = 12.sp) }
                }
                Spacer(Modifier.height(10.dp))
                state.history.take(20).forEach { item ->
                    HistoryCardNew(item = item)
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Spacer(Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF0E0E12)).padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\ud83d\udcc2", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("\u0645\u0627 \u0641\u064a \u0623\u0639\u0645\u0627\u0644 \u0633\u0627\u0628\u0642\u0629 \u0628\u0639\u062f", color = TextSecondary, fontSize = 13.sp)
                        Text("\u0627\u0628\u062f\u0623 \u0628\u0625\u0646\u0634\u0627\u0621 \u0623\u0648\u0644 \u0623\u063a\u0646\u064a\u0629 \u0623\u0648 \u0641\u064a\u062f\u064a\u0648", color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    notifyMessage?.let { msg ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier.padding(bottom = 100.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF0A0A0A))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Text(text = msg, color = if (notifySuccess) Color(0xFF7FE58F) else Color(0xFFE85C5C), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SegmentedTab(emoji: String, label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor by animateColorAsState(targetValue = if (active) Accent else Color.Transparent, animationSpec = tween(200), label = "segBg")
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(bgColor).clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(label, color = if (active) Color.White else TextSecondary, fontSize = 14.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: String, content: @Composable RowScope.() -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(Modifier.width(6.dp))
            Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun NiceChip(label: String, active: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(targetValue = if (active) Accent else Color(0xFF14141A), animationSpec = tween(180), label = "chipBg")
    val borderColor = if (active) Accent else Color(0xFF232330)
    Box(
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (active) Color.White else TextSecondary, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun ResultCardNew(
    icon: String,
    title: String,
    subtitle: String,
    url: String,
    type: String,
    onSave: (String, String) -> Unit,
    onRegenerate: () -> Unit,
    lyrics: String?,
) {
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF0E0E12))
            .border(1.dp, Accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF14141A)).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(icon, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (subtitle.isNotBlank()) Text(subtitle, color = TextTertiary, fontSize = 11.sp)
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF22C55E).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("\u2705 \u062c\u0627\u0647\u0632", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(Modifier.padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionBtnNew("\u25b6\ufe0f", "\u0645\u0634\u0627\u0647\u062f\u0629/\u0633\u0645\u0627\u0639", primary = true) {
                        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                    }
                    ActionBtnNew("\ud83d\udce5", "\u062d\u0641\u0638", primary = false) { onSave(url, type) }
                    ActionBtnNew("\ud83d\udd17", "\u0645\u0634\u0627\u0631\u0643\u0629", primary = false) {
                        try {
                            val i = Intent(Intent.ACTION_SEND).apply {
                                type = if (type == "video") "video/*" else "audio/*"
                                putExtra(Intent.EXTRA_TEXT, url)
                            }
                            ctx.startActivity(Intent.createChooser(i, null))
                        } catch (_: Exception) {}
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionBtnNew("\ud83d\udd04", "\u0625\u0639\u0627\u062f\u0629 \u062a\u0648\u0644\u064a\u062f", primary = false) { onRegenerate() }
                    ActionBtnNew("\ud83d\udccb", "\u0646\u0633\u062e \u0627\u0644\u0631\u0627\u0628\u0637", primary = false) {
                        clip.setText(AnnotatedString(url))
                    }
                }

                if (!lyrics.isNullOrBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF14141A)).padding(12.dp),
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\ud83d\udcdd", fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                Text("\u0643\u0644\u0645\u0627\u062a \u0627\u0644\u0623\u063a\u0646\u064a\u0629", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(lyrics, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBtnNew(emoji: String, label: String, primary: Boolean, onClick: () -> Unit) {
    val bg = if (primary) AccentGradient else Brush.linearGradient(listOf(Color(0xFF1A1A22), Color(0xFF1A1A22)))
    Box(
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(bg)
            .border(width = if (primary) 0.dp else 1.dp, color = if (primary) Color.Transparent else Color(0xFF232330), shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 12.sp)
            Spacer(Modifier.width(5.dp))
            Text(label, color = if (primary) Color.White else TextSecondary, fontSize = 12.sp, fontWeight = if (primary) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun HistoryCardNew(item: MediaHistoryItem) {
    val ctx = LocalContext.current
    val typeIcon = when (item.type.lowercase()) {
        "song" -> "\ud83c\udfb5"
        "video" -> "\ud83c\udfac"
        "image" -> "\ud83d\uddbc\ufe0f"
        else -> "\ud83d\udcc1"
    }
    val typeLabel = when (item.type.lowercase()) {
        "song" -> "\u0623\u063a\u0646\u064a\u0629"
        "video" -> "\u0641\u064a\u062f\u064a\u0648"
        "image" -> "\u0635\u0648\u0631\u0629"
        else -> item.type
    }
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF0E0E12))
            .border(1.dp, Color(0xFF1F1F26), RoundedCornerShape(14.dp))
            .clickable { try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url))) } catch (_: Exception) {} },
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Accent.copy(alpha = 0.12f))
                    .border(1.dp, Accent.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(typeIcon, fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title.ifBlank { item.prompt }.take(40),
                    color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF1A1A22)).padding(horizontal = 6.dp, vertical = 2.dp),
                    ) { Text(typeLabel, color = TextTertiary, fontSize = 10.sp) }
                    Spacer(Modifier.width(6.dp))
                    Text(formatTimeAgo(item.createdAt), color = TextTertiary, fontSize = 10.sp)
                }
            }
            Text("\u203a", color = TextTertiary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTimeAgo(ts: Double): String {
    if (ts <= 0.0) return ""
    val diff = (System.currentTimeMillis() / 1000.0 - ts).toLong()
    return when {
        diff < 60 -> "\u0627\u0644\u0622\u0646"
        diff < 3600 -> "${diff / 60} \u062f"
        diff < 86400 -> "${diff / 3600} \u0633"
        diff < 604800 -> "${diff / 86400} \u064a"
        else -> "${diff / 604800} \u0623"
    }
}
