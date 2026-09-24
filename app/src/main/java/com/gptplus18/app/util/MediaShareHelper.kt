package com.gptplus18.app.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * مساعد التحميل والمشاركة — بدون فتح المتصفح
 * - saveToGallery: حفظ مباشر في المعرض (بدون DownloadManager)
 * - shareMedia: تنزيل إلى cache ثم مشاركة الملف نفسه
 */
object MediaShareHelper {

    /**
     * ⭐ يستخرج الامتداد الحقيقي من الرابط (png/jpg/webp/...)
     * fallback: القيمة الافتراضية حسب النوع
     */
    private fun extFor(url: String, type: String): String {
        val path = url.substringBefore('?').lowercase()
        val candidates: List<String> = when (type) {
            "image" -> listOf("png", "jpg", "jpeg", "webp", "gif")
            "song"  -> listOf("mp3", "m4a", "wav", "ogg")
            "video" -> listOf("mp4", "webm", "mov")
            else    -> emptyList()
        }
        for (e in candidates) {
            if (path.endsWith(".$e")) return e
        }
        return when (type) {
            "image" -> "jpg"
            "song"  -> "mp3"
            "video" -> "mp4"
            else    -> "bin"
        }
    }

    /** يحدد MIME الصحيح بناءً على الامتداد الفعلي */
    private fun mimeFor(name: String, type: String): String {
        val n = name.lowercase()
        return when {
            n.endsWith(".png")  -> "image/png"
            n.endsWith(".jpg") || n.endsWith(".jpeg") -> "image/jpeg"
            n.endsWith(".webp") -> "image/webp"
            n.endsWith(".gif")  -> "image/gif"
            n.endsWith(".mp3")  -> "audio/mpeg"
            n.endsWith(".m4a")  -> "audio/mp4"
            n.endsWith(".wav")  -> "audio/wav"
            n.endsWith(".ogg")  -> "audio/ogg"
            n.endsWith(".mp4")  -> "video/mp4"
            n.endsWith(".webm") -> "video/webm"
            n.endsWith(".mov")  -> "video/quicktime"
            type == "image" -> "image/jpeg"
            type == "song"  -> "audio/mpeg"
            type == "video" -> "video/mp4"
            else -> "application/octet-stream"
        }
    }

    private fun subDirFor(type: String): String = when (type) {
        "image" -> Environment.DIRECTORY_PICTURES
        "song" -> Environment.DIRECTORY_MUSIC
        "video" -> Environment.DIRECTORY_MOVIES
        else -> Environment.DIRECTORY_DOWNLOADS
    }

    /**
     * ⭐ الحفظ المباشر — يشتغل مثل ChatGPT / Facebook
     * - بدون DownloadManager (ما يحتاج Wi-Fi)
     * - يحفظ مباشرة في المعرض
     * - Toast واحد فقط في النهاية
     */
    /**
     * ⭐ الحفظ مع callback (بدون Toast)
     * @param onResult callback يُستدعى بالنتيجة: true = نجح، false = فشل
     */
    fun saveToGallery(
        context: Context,
        url: String,
        type: String,
        onResult: ((Boolean) -> Unit)? = null,
    ) {
        try {
            val ext = extFor(url, type)
            val name = "GPT18_${type}_${System.currentTimeMillis()}.$ext"
            val subDir = subDirFor(type)

            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val result = downloadAndSave(context, url, type, name, subDir)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult?.invoke(result)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaShareHelper", "saveToGallery error: ${e.message}")
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult?.invoke(false)
            }
        }
    }

    private suspend fun downloadAndSave(
        context: Context,
        url: String,
        type: String,
        name: String,
        subDir: String,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1) نزّل البايتات
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 120_000
                requestMethod = "GET"
                instanceFollowRedirects = true
            }
            val bytes: ByteArray = conn.inputStream.use { it.readBytes() }
            conn.disconnect()
            if (bytes.isEmpty()) return@withContext false

            // 2) احفظ حسب API
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ — MediaStore
                val mime = mimeFor(name, type)
                val collection = when (type) {
                    "image" -> android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "song"  -> android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    "video" -> android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else    -> android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
                }
                // ⭐ ترتيب صحيح: المجلد الرئيسي/اسم التطبيق
                //    مثال: "Pictures/GPT+18" أو "Music/GPT+18"
                val relativePath = "$subDir/GPT+18"
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(collection, values) ?: return@withContext false
                try {
                    resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    // ⭐ نعلن اكتمال الحفظ
                    values.clear()
                    values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    true
                } catch (e: Exception) {
                    // نحذف الإدخال الفاشل
                    try { resolver.delete(uri, null, null) } catch (_: Exception) {}
                    android.util.Log.e("MediaShareHelper", "write failed: ${e.message}")
                    false
                }
            } else {
                // Android 9- — حفظ مباشر في المجلد العام
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(subDir),
                    "GPT+18",
                )
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, name)
                file.outputStream().use { it.write(bytes) }
                // نعلم النظام بالملف الجديد
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    null,
                    null,
                )
                true
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaShareHelper", "downloadAndSave فشل: ${e.message}")
            false
        }
    }

    /**
     * مشاركة الملف نفسه (صورة/صوت/فيديو) — ليس الرابط
     * ننزّل إلى cache ثم نشارك عبر FileProvider
     */
    suspend fun shareMedia(context: Context, url: String, type: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val ext = extFor(url, type)
                val fileName = "share_${System.currentTimeMillis()}.$ext"
                val cacheDir = File(context.cacheDir, "shared_media")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val outFile = File(cacheDir, fileName)

                // تنزيل بسيط
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 60_000
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                }
                conn.inputStream.use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                conn.disconnect()

                if (!outFile.exists() || outFile.length() == 0L) {
                    return@withContext false
                }

                // FileProvider URI
                val authority = "${context.packageName}.fileprovider"
                val uri: Uri = FileProvider.getUriForFile(context, authority, outFile)

                val mime = when (type) {
                    "image" -> "image/*"
                    "song" -> "audio/*"
                    "video" -> "video/*"
                    else -> "*/*"
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    this.type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "مشاركة").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                true
            } catch (e: Exception) {
                false
            }
        }
}
