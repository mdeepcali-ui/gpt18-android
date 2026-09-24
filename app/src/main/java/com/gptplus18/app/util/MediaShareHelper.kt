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
 * - saveToGallery: DownloadManager (يشمل إشعار + معرض)
 * - shareMedia: تنزيل إلى cache ثم مشاركة الملف نفسه
 */
object MediaShareHelper {

    private fun extFor(type: String): String = when (type) {
        "image" -> "jpg"
        "song" -> "mp3"
        "video" -> "mp4"
        else -> "bin"
    }

    private fun subDirFor(type: String): String = when (type) {
        "image" -> Environment.DIRECTORY_PICTURES
        "song" -> Environment.DIRECTORY_MUSIC
        "video" -> Environment.DIRECTORY_MOVIES
        else -> Environment.DIRECTORY_DOWNLOADS
    }

    /**
     * حفظ الملف في معرض/موسيقى الجهاز (بدون متصفح)
     * ✅ يعرض Toast للمستخدم في كل الحالات
     * ✅ يعمل على Android 10+ بدون permission
     * ✅ يجرّب DownloadManager أولاً ثم fallback إلى تحميل يدوي
     */
    fun saveToGallery(context: Context, url: String, type: String) {
        try {
            val ext = extFor(type)
            val name = "GPT18_${type}_${System.currentTimeMillis()}.$ext"
            val subDir = subDirFor(type)

            // ─── محاولة 1: DownloadManager ───
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle(name)
                    .setDescription("GPT+18 — جاري التحميل...")
                    .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    .setDestinationInExternalPublicDir(subDir, name)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                android.widget.Toast.makeText(
                    context,
                    "✅ بدأ التحميل — تحقق من الإشعارات",
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
                return
            } catch (e: Exception) {
                // فشل DownloadManager — نجرّب fallback
                android.util.Log.w("MediaShareHelper", "DownloadManager فشل: ${e.message}")
            }

            // ─── محاولة 2 (fallback): تحميل يدوي + MediaStore ───
            android.widget.Toast.makeText(
                context,
                "⏳ جاري التحميل...",
                android.widget.Toast.LENGTH_SHORT,
            ).show()

            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val result = downloadAndSave(context, url, type, name, subDir)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        if (result) "✅ تم الحفظ في المجلد GPT+18/$subDir" else "❌ فشل التحميل",
                        android.widget.Toast.LENGTH_LONG,
                    ).show()
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "❌ خطأ: ${e.message ?: "غير معروف"}",
                android.widget.Toast.LENGTH_LONG,
            ).show()
        }
    }

    /**
     * تحميل يدوي + حفظ عبر MediaStore (Android 10+)
     * أو في المجلد العام (Android 9-)
     */
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
                val mime = when (type) {
                    "image" -> "image/jpeg"
                    "song"  -> "audio/mpeg"
                    "video" -> "video/mp4"
                    else    -> "application/octet-stream"
                }
                val collection = when (type) {
                    "image" -> android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "song"  -> android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    "video" -> android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else    -> android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
                }
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "GPT+18/$subDir")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(collection, values) ?: return@withContext false
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                true
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
                val ext = extFor(type)
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
