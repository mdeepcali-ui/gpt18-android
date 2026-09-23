package com.gptplus18.app.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
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
     */
    fun saveToGallery(context: Context, url: String, type: String) {
        try {
            val ext = extFor(type)
            val name = "GPT18_${type}_${System.currentTimeMillis()}.$ext"
            val subDir = subDirFor(type)

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(name)
                .setDescription("جاري التحميل...")
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setDestinationInExternalPublicDir("GPT+18/$subDir", name)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        } catch (_: Exception) { }
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
