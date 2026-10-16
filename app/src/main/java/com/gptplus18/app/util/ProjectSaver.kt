package com.gptplus18.app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * ProjectSaver — يحفظ مشاريع المساعد على موبايل العميل.
 *
 * Android 10+ (Scoped Storage):
 * - يستخدم MediaStore.Downloads — بلا permission صريح
 * - ينشئ مجلد باسم المشروع داخل Downloads/
 *
 * Android <10:
 * - يستخدم Environment.getExternalStoragePublicDirectory
 * - يحتاج WRITE_EXTERNAL_STORAGE
 */
object ProjectSaver {

    data class Result(
        val success: Boolean,
        val savedCount: Int = 0,
        val folderPath: String = "",
        val error: String = "",
    )

    data class BinaryFile(
        val base64: String,
        val mimeType: String,
    )

    fun saveProject(
        context: Context,
        folderName: String,
        files: Map<String, String>,
        binaries: Map<String, BinaryFile> = emptyMap(),
        totalBytes: Long = 0L,
    ): Result {
        if (files.isEmpty() && binaries.isEmpty()) {
            return Result(false, error = "لا ملفات للحفظ")
        }
        val cleanFolder = folderName.replace(Regex("[^\\w\\-_.]"), "_").take(64)

        // نحسب الحجم لو مو مُمرَّر
        val actualBytes = if (totalBytes > 0L) totalBytes else {
            files.values.sumOf { it.toByteArray(Charsets.UTF_8).size.toLong() } +
            binaries.values.sumOf { (it.base64.length * 3L) / 4L }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveMediaStore(context, cleanFolder, files, binaries, actualBytes)
        } else {
            saveLegacy(context, cleanFolder, files, binaries, actualBytes)
        }
    }

    // ═══════════════════════════════════════════════════════
    // Android 10+ — MediaStore
    // ═══════════════════════════════════════════════════════
    private fun saveMediaStore(
        context: Context,
        folderName: String,
        files: Map<String, String>,
        binaries: Map<String, BinaryFile>,
        totalBytes: Long,
    ): Result {
        var saved = 0
        val relativeDir = "${Environment.DIRECTORY_DOWNLOADS}/$folderName"
        try {
            // ملفات نصية
            for ((path, content) in files) {
                if (saveOneMediaStore(context, relativeDir, path, content.toByteArray(Charsets.UTF_8), guessMime(path))) {
                    saved++
                }
            }
            // ملفات ثنائية
            for ((path, bf) in binaries) {
                try {
                    val bytes = android.util.Base64.decode(bf.base64, android.util.Base64.DEFAULT)
                    if (saveOneMediaStore(context, relativeDir, path, bytes, bf.mimeType)) {
                        saved++
                    }
                } catch (_: Exception) {}
            }

            return if (saved > 0) {
                registerProject(context, folderName, "Downloads/$folderName", saved, totalBytes)
                Result(true, savedCount = saved, folderPath = "Downloads/$folderName")
            } else {
                Result(false, error = "لم يُحفظ أي ملف")
            }
        } catch (e: Exception) {
            return Result(false, error = e.message ?: "خطأ في MediaStore")
        }
    }

    private fun saveOneMediaStore(
        context: Context,
        relativeDir: String,
        path: String,
        bytes: ByteArray,
        mime: String,
    ): Boolean {
        val cleanPath = path.replace("\\", "/").trimStart('/')
        if (cleanPath.isEmpty()) return false
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, cleanPath.substringAfterLast('/'))
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri: Uri? = context.contentResolver.insert(collection, values)
        if (uri == null) return false
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out -> out.write(bytes) }
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            true
        } catch (e: Exception) {
            try { context.contentResolver.delete(uri, null, null) } catch (_: Exception) {}
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    // Android <10 — Legacy
    // ═══════════════════════════════════════════════════════
    @Suppress("DEPRECATION")
    private fun saveLegacy(
        context: Context,
        folderName: String,
        files: Map<String, String>,
        binaries: Map<String, BinaryFile>,
        totalBytes: Long,
    ): Result {
        return try {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val projectDir = File(root, folderName)
            if (!projectDir.exists()) projectDir.mkdirs()

            var saved = 0
            for ((path, content) in files) {
                val cleanPath = path.replace("\\", "/").trimStart('/')
                if (cleanPath.isEmpty()) continue
                val target = File(projectDir, cleanPath)
                target.parentFile?.mkdirs()
                target.writeText(content, Charsets.UTF_8)
                saved++
            }
            for ((path, bf) in binaries) {
                try {
                    val bytes = android.util.Base64.decode(bf.base64, android.util.Base64.DEFAULT)
                    val cleanPath = path.replace("\\", "/").trimStart('/')
                    if (cleanPath.isEmpty()) continue
                    val target = File(projectDir, cleanPath)
                    target.parentFile?.mkdirs()
                    target.writeBytes(bytes)
                    saved++
                } catch (_: Exception) {}
            }

            if (saved > 0) {
                registerProject(context, folderName, "Downloads/$folderName", saved, totalBytes)
                Result(true, savedCount = saved, folderPath = "Downloads/$folderName")
            } else {
                Result(false, error = "لم يُحفظ أي ملف")
            }
        } catch (e: Exception) {
            Result(false, error = e.message ?: "خطأ في الحفظ")
        }
    }

    private fun registerProject(
        context: android.content.Context,
        folderName: String,
        folderPath: String,
        filesCount: Int,
        totalBytes: Long,
    ) {
        try {
            val prefs = context.getSharedPreferences("projects_registry", android.content.Context.MODE_PRIVATE)
            val gson = com.google.gson.Gson()
            val raw = prefs.getString("projects_v1", null)
            val type = object : com.google.gson.reflect.TypeToken<List<com.gptplus18.app.data.local.SavedProject>>() {}.type
            val current: MutableList<com.gptplus18.app.data.local.SavedProject> = mutableListOf()
            if (raw != null) {
                val parsed = gson.fromJson<List<com.gptplus18.app.data.local.SavedProject>>(raw, type)
                if (parsed != null) current.addAll(parsed)
            }
            current.removeAll { it.folderName == folderName }
            current.add(0, com.gptplus18.app.data.local.SavedProject(
                folderName = folderName,
                folderPath = folderPath,
                filesCount = filesCount,
                totalBytes = totalBytes,
                savedAt = System.currentTimeMillis(),
            ))
            val trimmed = if (current.size > 200) current.take(200) else current
            prefs.edit().putString("projects_v1", gson.toJson(trimmed)).apply()
        } catch (_: Exception) {}
    }

    private fun guessMime(path: String): String {
        return when (path.substringAfterLast('.', "").lowercase()) {
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "md" -> "text/markdown"
            "txt" -> "text/plain"
            "py" -> "text/x-python"
            "kt" -> "text/plain"
            "java" -> "text/x-java"
            "xml" -> "text/xml"
            "yml", "yaml" -> "text/yaml"
            "sh" -> "text/x-shellscript"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "svg" -> "image/svg+xml"
            else -> "application/octet-stream"
        }
    }
}
