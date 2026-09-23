package com.gptplus18.app.ui.screens.media

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Edit
import coil.compose.AsyncImage
import com.gptplus18.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(vm: MediaViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) vm.setEditImage(uri)
    }

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = { Text("إنشاء الوسائط", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Tabs — 3 خيارات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TabCard("صورة", state.tab == MediaTab.IMAGE, Modifier.weight(1f)) {
                    vm.setTab(MediaTab.IMAGE)
                }
                TabCard("أغنية", state.tab == MediaTab.SONG, Modifier.weight(1f)) {
                    vm.setTab(MediaTab.SONG)
                }
                TabCard("فيديو", state.tab == MediaTab.VIDEO, Modifier.weight(1f)) {
                    vm.setTab(MediaTab.VIDEO)
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.prompt,
                onValueChange = { vm.setPrompt(it) },
                placeholder = {
                    Text(
                        when (state.tab) {
                            MediaTab.IMAGE -> "اوصف الصورة..."
                            MediaTab.SONG -> "اوصف الأغنية..."
                            MediaTab.VIDEO -> "اوصف الفيديو..."
                        },
                        color = TextTertiary,
                    )
                },
                label = { Text("الوصف") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    cursorColor = Accent,
                ),
            )

            Spacer(Modifier.height(12.dp))

            // إعدادات حسب التبويب
            when (state.tab) {
                MediaTab.IMAGE -> {
                    Text("الأبعاد", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "square" to "مربع",
                            "portrait" to "طولي",
                            "landscape" to "عريض",
                        ).forEach { (k, label) ->
                            PresetChip(label, state.preset == k) { vm.setPreset(k) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("أو عدّل صورة موجودة", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))

                    if (state.editImageUri == null) {
                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BgTertiary,
                                contentColor = TextPrimary,
                            ),
                        ) {
                            Icon(Icons.Default.Image, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("اختر صورة للتعديل", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(0.5.dp, BubbleBorder, RoundedCornerShape(12.dp)),
                        ) {
                            AsyncImage(
                                model = state.editImageUri,
                                contentDescription = "صورة للتعديل",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Error.copy(alpha = 0.9f))
                                    .clickable { vm.clearEditImage() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Close, "إزالة", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                MediaTab.SONG -> {
                    Text("المدة", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(30, 60, 120, 240).forEach { d ->
                            PresetChip("$d ث", state.duration == d) { vm.setDuration(d) }
                        }
                    }
                }
                MediaTab.VIDEO -> {
                    Text("المدة", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 15, 30).forEach { d ->
                            PresetChip("$d ث", state.videoDuration == d) {
                                vm.setVideoDuration(d)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("النموذج", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "auto" to "تلقائي",
                            "ltx-2.3-spicy" to "LTX",
                            "wan-2.2-spicy" to "Wan",
                            "grok-imagine" to "Grok",
                        ).forEach { (k, label) ->
                            PresetChip(label, state.videoModel == k) {
                                vm.setVideoModel(k)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            val isEditMode = state.tab == MediaTab.IMAGE && state.editImageUri != null
            Button(
                onClick = { if (isEditMode) vm.editImage() else vm.generate() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = BgPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.tab == MediaTab.VIDEO) "جاري التوليد (قد يأخذ دقائق)..."
                        else if (isEditMode) "جاري التعديل..."
                        else "جاري الإنشاء...",
                        color = BgPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                } else {
                    if (isEditMode) {
                        Icon(Icons.Default.Edit, null, tint = BgPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("تعديل", color = BgPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text("إنشاء", color = BgPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            state.error?.let { err ->
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(err, color = Error, modifier = Modifier.padding(14.dp), fontSize = 14.sp)
                }
            }

            // ─── نتيجة الصورة ───
            if (state.tab == MediaTab.IMAGE && state.imageUrl != null) {
                Spacer(Modifier.height(20.dp))
                ResultCard {
                    AsyncImage(
                        model = state.imageUrl,
                        contentDescription = "Generated",
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.height(10.dp))
                    CopyRow("رابط الصورة", state.imageUrl!!)
                }
            }

            // ─── نتيجة الأغنية ───
            if (state.tab == MediaTab.SONG && state.songUrl != null) {
                Spacer(Modifier.height(20.dp))
                ResultCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = Accent, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(state.songTitle ?: "أغنية", color = TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    CopyRow("رابط الأغنية", state.songUrl!!)
                    state.songLyrics?.let {
                        Spacer(Modifier.height(10.dp))
                        Text("الكلمات:", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(it, color = TextSecondary, fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // ─── نتيجة الفيديو ───
            if (state.tab == MediaTab.VIDEO && state.videoUrl != null) {
                Spacer(Modifier.height(20.dp))
                ResultCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = Accent, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("فيديو جاهز", color = TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    state.videoModelUsed?.let {
                        Text("النموذج: $it", color = TextTertiary, fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    CopyRow("رابط الفيديو", state.videoUrl!!)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            try {
                                ctx.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(state.videoUrl!!)),
                                )
                            } catch (_: Exception) { }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = BgPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text("فتح الفيديو", color = BgPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ResultCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
private fun TabCard(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) Accent else BgSecondary,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text,
                color = if (active) BgPrimary else TextPrimary,
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PresetChip(label: String, active: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (active) Accent else BgTertiary,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            label,
            color = if (active) BgPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun CopyRow(label: String, value: String) {
    val clip = LocalClipboardManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = { clip.setText(AnnotatedString(value)) }) {
            Icon(Icons.Default.ContentCopy, "نسخ", tint = Accent)
        }
    }
}
