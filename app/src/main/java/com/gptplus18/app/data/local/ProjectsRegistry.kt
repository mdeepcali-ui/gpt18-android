package com.gptplus18.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ProjectsRegistry — سجل محلي للمشاريع المحفوظة في Downloads.
 * يخزن: الاسم، المسار، عدد الملفات، الحجم، التاريخ.
 */
data class SavedProject(
    val folderName: String,
    val folderPath: String,
    val filesCount: Int,
    val totalBytes: Long,
    val savedAt: Long,
    val sourceUrl: String = "",
)

@Singleton
class ProjectsRegistry @Inject constructor(
    private val context: Context,
) {
    companion object {
        private const val PREFS = "projects_registry"
        private const val KEY = "projects_v1"
        private const val MAX_ENTRIES = 200
    }

    private val gson = Gson()

    private fun prefs() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun all(): List<SavedProject> {
        val raw = prefs().getString(KEY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SavedProject>>() {}.type
            gson.fromJson<List<SavedProject>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(project: SavedProject) {
        val current = all().toMutableList()
        // نزيل أي نسخة قديمة بنفس الاسم
        current.removeAll { it.folderName == project.folderName }
        current.add(0, project)
        val trimmed = if (current.size > MAX_ENTRIES) current.take(MAX_ENTRIES) else current
        save(trimmed)
    }

    fun remove(folderName: String) {
        val current = all().toMutableList()
        current.removeAll { it.folderName == folderName }
        save(current)
    }

    fun rename(oldName: String, newName: String): Boolean {
        if (newName.isBlank()) return false
        val clean = newName.replace(Regex("[^\\w\\-_.]"), "_").take(64)
        if (clean.isBlank()) return false
        val current = all().toMutableList()
        val idx = current.indexOfFirst { it.folderName == oldName }
        if (idx == -1) return false
        // لو الاسم الجديد موجود لمشروع آخر → نرفض
        if (current.any { it.folderName == clean }) return false
        val p = current[idx]
        current[idx] = p.copy(folderName = clean, folderPath = p.folderPath.replace(oldName, clean))
        save(current)
        return true
    }

    fun updateMeta(folderName: String, filesCount: Int, totalBytes: Long) {
        val current = all().toMutableList()
        val idx = current.indexOfFirst { it.folderName == folderName }
        if (idx == -1) return
        current[idx] = current[idx].copy(filesCount = filesCount, totalBytes = totalBytes)
        save(current)
    }

    fun clear() {
        prefs().edit().remove(KEY).apply()
    }

    private fun save(list: List<SavedProject>) {
        try {
            prefs().edit().putString(KEY, gson.toJson(list)).apply()
        } catch (_: Exception) {}
    }
}
