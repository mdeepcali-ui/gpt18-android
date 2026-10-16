package com.gptplus18.app.ui.screens.projects

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.data.local.ProjectsRegistry
import com.gptplus18.app.data.local.SavedProject
import com.gptplus18.app.ui.theme.LocalAppColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.outlined.Folder
import com.gptplus18.app.ui.theme.ScheherazadeFontFamily

private val AccentBlue = Color(0xFF3B82F6)
private val Danger = Color(0xFFEF4444)

enum class SortMode { RECENT, NAME, SIZE }

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ProjectsScreen(
    onBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val colors = LocalAppColors.current
    val registry = remember { ProjectsRegistry(ctx) }

    var projects by remember { mutableStateOf(registry.all()) }
    var sortMode by remember { mutableStateOf(SortMode.RECENT) }
    var confirmDelete by remember { mutableStateOf<SavedProject?>(null) }
    var renameTarget by remember { mutableStateOf<SavedProject?>(null) }

    fun refresh() { projects = registry.all() }

    val sorted = remember(projects, sortMode) {
        when (sortMode) {
            SortMode.RECENT -> projects.sortedByDescending { it.savedAt }
            SortMode.NAME -> projects.sortedBy { it.folderName.lowercase() }
            SortMode.SIZE -> projects.sortedByDescending { it.totalBytes }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مشاريعي", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع", tint = colors.textPrimary)
                    }
                },
                actions = {
                    if (projects.isNotEmpty()) {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { showMenu = true }) {
                                Text(
                                    when (sortMode) {
                                        SortMode.RECENT -> "الأحدث"
                                        SortMode.NAME -> "أبجدي"
                                        SortMode.SIZE -> "الحجم"
                                    },
                                    color = AccentBlue,
                                    fontSize = 9.6.sp,
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = colors.surface,
                            ) {
                                DropdownMenuItem(
                                    text = { Text("الأحدث أولاً", color = colors.textPrimary) },
                                    onClick = { sortMode = SortMode.RECENT; showMenu = false },
                                )
                                DropdownMenuItem(
                                    text = { Text("أبجدياً", color = colors.textPrimary) },
                                    onClick = { sortMode = SortMode.NAME; showMenu = false },
                                )
                                DropdownMenuItem(
                                    text = { Text("الأكبر حجماً", color = colors.textPrimary) },
                                    onClick = { sortMode = SortMode.SIZE; showMenu = false },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )
        },
        containerColor = colors.bg,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.bg),
        ) {
            if (sorted.isEmpty()) {
                EmptyProjects(colors.textPrimary, colors.textSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(sorted, key = { it.folderName }) { p ->
                        ProjectCard(
                            project = p,
                            colors = colors,
                            onOpen = { openFolderInDownloads(ctx, p.folderName) },
                            onRename = { renameTarget = p },
                            onDelete = { confirmDelete = p },
                        )
                    }
                }
            }

            confirmDelete?.let { p ->
                AlertDialog(
                    onDismissRequest = { confirmDelete = null },
                    title = { Text("حذف المشروع؟", color = colors.textPrimary) },
                    text = {
                        Text(
                            "اختر ما تريد:\n\n1) حذف السجل فقط — الملفات تبقى\n2) حذف كل شي — السجل + الملفات",
                            color = colors.textSecondary,
                            fontSize = 9.6.sp,
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            // حذف السجل + الملفات
                            try {
                                val dir = File(
                                    android.os.Environment.getExternalStoragePublicDirectory(
                                        android.os.Environment.DIRECTORY_DOWNLOADS
                                    ),
                                    p.folderName,
                                )
                                dir.deleteRecursively()
                            } catch (_: Exception) {}
                            registry.remove(p.folderName)
                            refresh()
                            confirmDelete = null
                        }) { Text("حذف الكل", color = Danger) }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = {
                                registry.remove(p.folderName)
                                refresh()
                                confirmDelete = null
                            }) { Text("السجل فقط", color = colors.textSecondary) }

                            TextButton(onClick = { confirmDelete = null }) {
                                Text("إلغاء", color = colors.textSecondary)
                            }
                        }
                    },
                    containerColor = colors.surface,
                )
            }

            renameTarget?.let { p ->
                RenameDialog(
                    initial = p.folderName,
                    colors = colors,
                    onDismiss = { renameTarget = null },
                    onConfirm = { newName ->
                        val ok = registry.rename(p.folderName, newName)
                        // نعيد تسمية المجلد الفعلي أيضاً
                        if (ok) {
                            try {
                                val root = android.os.Environment.getExternalStoragePublicDirectory(
                                    android.os.Environment.DIRECTORY_DOWNLOADS
                                )
                                val oldDir = File(root, p.folderName)
                                val newDir = File(root, newName.replace(Regex("[^\\w\\-_.]"), "_").take(64))
                                if (oldDir.exists()) oldDir.renameTo(newDir)
                            } catch (_: Exception) {}
                            refresh()
                        }
                        renameTarget = null
                    },
                )
            }
        }
    }
}

@Composable
private fun RenameDialog(
    initial: String,
    colors: com.gptplus18.app.ui.theme.AppColors,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعادة تسمية", color = colors.textPrimary) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.bg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { if (it.length <= 64) name = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = ScheherazadeFontFamily, color = colors.textPrimary, fontSize = 10.4.sp),
                    cursorBrush = SolidColor(AccentBlue),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("حفظ", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = colors.textSecondary)
            }
        },
        containerColor = colors.surface,
    )
}

@Composable
private fun EmptyProjects(textPrimary: Color, textSecondary: Color) {
    com.gptplus18.app.ui.components.EmptyState(
        icon = androidx.compose.material.icons.Icons.Outlined.Folder,
        title = "ما في مشاريع محفوظة بعد",
        description = "لما شريكك الذكي يحفظلك مشروع على موبايلك، رح يظهر هون",
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ProjectCard(
    project: SavedProject,
    colors: com.gptplus18.app.ui.theme.AppColors,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateStr = remember(project.savedAt) {
        try {
            SimpleDateFormat("d MMM HH:mm", Locale("ar")).format(Date(project.savedAt))
        } catch (_: Exception) { "" }
    }

    val sizeStr = remember(project.totalBytes) {
        when {
            project.totalBytes >= 1_000_000 -> "%.1f MB".format(project.totalBytes / 1_000_000.0)
            project.totalBytes >= 1_000 -> "%.1f KB".format(project.totalBytes / 1_000.0)
            project.totalBytes > 0 -> "${project.totalBytes} B"
            else -> ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onRename,
            )
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📦", fontSize = 16.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        project.folderName,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.2.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    Spacer(Modifier.height(2.dp))
                    val details = buildString {
                        append("${project.filesCount} ملف")
                        if (sizeStr.isNotBlank()) { append(" · "); append(sizeStr) }
                        if (dateStr.isNotBlank()) { append(" · "); append(dateStr) }
                    }
                    Text(details, color = colors.textSecondary, fontSize = 9.sp)
                }
                IconButton(onClick = onRename) {
                    Icon(Icons.Default.Edit, "إعادة تسمية", tint = AccentBlue, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "حذف", tint = Danger, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "المسار: " + project.folderPath,
                color = AccentBlue,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 📂 فتح مجلد المشروع في Downloads — طريقة موثوقة
// ═══════════════════════════════════════════════════════════
private fun openFolderInDownloads(ctx: android.content.Context, folderName: String) {
    val encoded = java.net.URLEncoder.encode("Download/$folderName", "UTF-8")
        .replace("+", "%20")
    val uri = android.net.Uri.parse(
        "content://com.android.externalstorage.documents/document/primary%3A$encoded"
    )

    // محاولة 1: DocumentsUI
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "vnd.android.document/directory")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(intent)
        return
    } catch (_: Exception) {}

    // محاولة 2: عام
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(intent)
        return
    } catch (_: Exception) {}

    // محاولة 3: نسخ المسار
    try {
        val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
        clip.setPrimaryClip(
            android.content.ClipData.newPlainText(
                "folder_path",
                "/storage/emulated/0/Download/$folderName"
            )
        )
        android.widget.Toast.makeText(
            ctx,
            "المسار منسوخ: Downloads/$folderName",
            android.widget.Toast.LENGTH_LONG,
        ).show()
    } catch (_: Exception) {
        android.widget.Toast.makeText(ctx, "تعذّر فتح المجلد", android.widget.Toast.LENGTH_SHORT).show()
    }
}
