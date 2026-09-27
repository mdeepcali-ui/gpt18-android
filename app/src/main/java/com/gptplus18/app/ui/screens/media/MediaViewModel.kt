package com.gptplus18.app.ui.screens.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.models.MediaHistoryItem
import com.gptplus18.app.data.repository.MediaRepository
import com.gptplus18.app.util.AnalyticsHelper
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MediaTab { SONG, VIDEO }

data class MediaUiState(
    val tab: MediaTab = MediaTab.SONG,
    val prompt: String = "",
    val duration: Int = 240,
    val videoDuration: Int = 5,
    val videoModel: String = "auto",
    val videoImageUri: String? = null,
    val videoImageUploading: Boolean = false,
    val videoImageUrl: String? = null,
    val isLoading: Boolean = false,
    val songUrl: String? = null,
    val songTitle: String? = null,
    val songLyrics: String? = null,
    val videoUrl: String? = null,
    val videoModelUsed: String? = null,
    val error: String? = null,
    // ⭐ M4-b: سجل الوسائط
    val history: List<MediaHistoryItem> = emptyList(),
    val historyLoading: Boolean = false,
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repo: MediaRepository,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(MediaUiState())
    
    init {
        loadHistory()
    }
    val state: StateFlow<MediaUiState> = _state.asStateFlow()

    private var currentJob: Job? = null

    fun setTab(t: MediaTab) {
        currentJob?.cancel()
        currentJob = null
        _state.value = _state.value.copy(
            tab = t,
            isLoading = false,
            error = null,
            songUrl = null,
            songTitle = null,
            songLyrics = null,
            videoUrl = null,
            videoModelUsed = null,
        )
    }

    fun setPrompt(v: String) { _state.value = _state.value.copy(prompt = v) }
    fun setDuration(d: Int) { _state.value = _state.value.copy(duration = d) }
    fun setVideoDuration(d: Int) { _state.value = _state.value.copy(videoDuration = d) }
    fun setVideoImage(uri: String) {
        _state.value = _state.value.copy(videoImageUri = uri, error = null)
    }

    fun clearVideoImage() {
        _state.value = _state.value.copy(videoImageUri = null, videoImageUrl = null)
    }

    private suspend fun uploadVideoImage(): String? {
        val uri = _state.value.videoImageUri ?: return null
        _state.value = _state.value.copy(videoImageUploading = true)
        return try {
            val r = repo.uploadTempImage(uri)
            _state.value = _state.value.copy(videoImageUploading = false)
            when (r) {
                is Result.Success -> {
                    val url = r.data
                    _state.value = _state.value.copy(videoImageUrl = url)
                    url
                }
                else -> {
                    _state.value = _state.value.copy(
                        videoImageUploading = false,
                        error = "\u0641\u0634\u0644 \u0631\u0641\u0639 \u0627\u0644\u0635\u0648\u0631\u0629",
                    )
                    null
                }
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(videoImageUploading = false)
            null
        }
    }

    fun setVideoModel(m: String) { _state.value = _state.value.copy(videoModel = m) }

    fun generate() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isBlank()) {
            _state.value = _state.value.copy(error = "اكتب وصف")
            return
        }

        val tabSnapshot = _state.value.tab
        val durationSnapshot = _state.value.duration
        val videoDurationSnapshot = _state.value.videoDuration
        val videoModelSnapshot = _state.value.videoModel

        currentJob?.cancel()

        currentJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                songUrl = null,
                songTitle = null,
                songLyrics = null,
                videoUrl = null,
                videoModelUsed = null,
            )

            when (tabSnapshot) {
                MediaTab.SONG -> {
                    when (val r = repo.generateSong(prompt, durationSnapshot)) {
                        is Result.Success -> {
                            analytics.logSongGenerated(durationSec = durationSnapshot)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                songUrl = r.data.audioUrl,
                                songTitle = r.data.title,
                                songLyrics = r.data.lyrics,
                            )
                            loadHistory()
                        }
                        is Result.Error -> _state.value = _state.value.copy(isLoading = false, error = r.message)
                        else -> {}
                    }
                }
                MediaTab.VIDEO -> {
                    // 🎨 رفع الصورة إن وجدت
                    val imgUrl = uploadVideoImage()
                    when (val r = repo.generateVideo(prompt, videoDurationSnapshot, videoModelSnapshot, imgUrl)) {
                        is Result.Success -> {
                            analytics.logVideoGenerated(
                                durationSec = videoDurationSnapshot,
                                model = r.data.modelUsed ?: videoModelSnapshot,
                            )
                            _state.value = _state.value.copy(
                                isLoading = false,
                                videoUrl = r.data.videoUrl,
                                videoModelUsed = r.data.modelUsed,
                            )
                            loadHistory()
                        }
                        is Result.Error -> _state.value = _state.value.copy(isLoading = false, error = r.message)
                        else -> {}
                    }
                }
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    /**
     * 🔄 إعادة توليد الصورة/الأغنية/الفيديو بنفس الوصف
     */
    fun regenerate() {
        val current = _state.value
        val p = current.prompt.trim()
        if (p.isBlank()) {
            _state.value = current.copy(error = "لا يوجد وصف لإعادة التوليد")
            return
        }
        // تنظيف النتيجة السابقة ثم إعادة التوليد
        _state.value = current.copy(
            songUrl = null,
            videoUrl = null,
            error = null,
        )
        generate()
    }

    // ⭐ M4-b: سجل الوسائط
    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(historyLoading = true)
            when (val r = repo.getHistory()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        history = r.data.items,
                        historyLoading = false,
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(historyLoading = false)
                }
                else -> _state.value = _state.value.copy(historyLoading = false)
            }
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            when (repo.deleteHistoryItem(id)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        history = _state.value.history.filterNot { it.id == id },
                    )
                }
                else -> {}
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            when (repo.clearHistory()) {
                is Result.Success -> _state.value = _state.value.copy(history = emptyList())
                else -> {}
            }
        }
    }

    override fun onCleared() {
        currentJob?.cancel()
        super.onCleared()
    }
}
