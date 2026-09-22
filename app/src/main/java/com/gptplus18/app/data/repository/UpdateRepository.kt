package com.gptplus18.app.data.repository

import com.gptplus18.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val latestVersion: Int,
    val latestName: String,
    val forceUpdate: Boolean,
    val downloadUrl: String,
    val changelog: String,
    val currentVersion: Int,
)

@Singleton
class UpdateRepository @Inject constructor() {

    private val updateUrl = "https://gptplus18.com/api/app/latest-version"

    suspend fun checkUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(updateUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode != 200) return@withContext null
            val text = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val j = JSONObject(text)
            val latest = j.optInt("version_code", 1)
            val current = BuildConfig.VERSION_CODE
            if (latest > current) {
                UpdateInfo(
                    latestVersion = latest,
                    latestName = j.optString("version_name", "1.0.0"),
                    forceUpdate = j.optBoolean("force", false),
                    downloadUrl = j.optString("download_url", "https://gptplus18.com/download"),
                    changelog = j.optString("changelog", ""),
                    currentVersion = current,
                )
            } else null
        } catch (_: Exception) { null }
    }
}
