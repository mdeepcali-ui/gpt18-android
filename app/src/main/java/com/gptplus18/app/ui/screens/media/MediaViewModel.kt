package com.gptplus18.app.ui.screens.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gptplus18.app.data.repository.MediaRepository
import com.gptplus18.app.util.AnalyticsHelper
import com.gptplus18.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MediaTab { IMAGE, SONG, VIDEO }

data class MediaUiState(
    val tab: MediaTab = MediaTab.IMAGE,
    val prompt: String = "",
    val preset: String = "square",
    val duration: Int = 240,
    val videoDuration: Int = 5,
    val videoModel: String = "auto",
    val isLoading: Boolean = false,
    val imageUrl: String? = null,
    val songUrl: String? = null,
    val songTitle: String? = null,
    val songLyrics: String? = null,
    val videoUrl: String? = null,
    val videoModelUsed: String? = null,
    val error: String? = null,
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repo: MediaRepository,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(MediaUiState())
    val state: StateFlow<MediaUiState> = _state.asStateFlow()

    fun setTab(t: MediaTab) {
        _state.value = _state.value.copy(
            tab = t,
            error = null,
            imageUrl = null,
            songUrl = null,
            songTitle = null,
            songLyrics = null,
            videoUrl = null,
            videoModelUsed = null,
        )
    }

    fun setPrompt(v: String) { _state.value = _state.value.copy(prompt = v) }
    fun setPreset(p: String) { _state.value = _state.value.copy(preset = p) }
    fun setDuration(d: Int) { _state.value = _state.value.copy(duration = d) }
    fun setVideoDuration(d: Int) { _state.value = _state.value.copy(videoDuration = d) }
    fun setVideoModel(m: String) { _state.value = _state.value.copy(videoModel = m) }

    fun generate() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isBlank()) {
            _state.value = _state.value.copy(error = "اكتب وصف")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                imageUrl = null,
                songUrl = null,
                songTitle = null,
                songLyrics = null,
                videoUrl = null,
                videoModelUsed = null,
            )
            when (_state.value.tab) {
                MediaTab.IMAGE -> {
                    val preset = _state.value.preset
                    when (val r = repo.generateImage(prompt, preset)) {
                        is Result.Success -> {
                            // 📊 Analytics
                            analytics.logImageGenerated(preset = preset)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                imageUrl = r.data.imageUrl,
                            )
                        }
                        is Result.Error -> _state.value = _state.value.copy(
                            isLoading = false,
                            error = r.message,
                        )
                        else -> {}
                    }
                }
                MediaTab.SONG -> {
                    val duration = _state.value.duration
                    when (val r = repo.generateSong(prompt, duration)) {
                        is Result.Success -> {
                            // 📊 Analytics
                            analytics.logSongGenerated(durationSec = duration)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                songUrl = r.data.audioUrl,
                                songTitle = r.data.title,
                                songLyrics = r.data.lyrics,
                            )
                        }
                        is Result.Error -> _state.value = _state.value.copy(
                            isLoading = false,
                            error = r.message,
                        )
                        else -> {}
                    }
                }
                MediaTab.VIDEO -> {
                    val duration = _state.value.videoDuration
                    val modelPref = _state.value.videoModel
                    when (val r = repo.generateVideo(prompt, duration, modelPref)) {
                        is Result.Success -> {
                            // 📊 Analytics
                            analytics.logVideoGenerated(
                                durationSec = duration,
                                model = r.data.modelUsed ?: modelPref,
                            )
                            _state.value = _state.value.copy(
                                isLoading = false,
                                videoUrl = r.data.videoUrl,
                                videoModelUsed = r.data.modelUsed,
                            )
                        }
                        is Result.Error -> _state.value = _state.value.copy(
                            isLoading = false,
                            error = r.message,
                        )
                        else -> {}
                    }
                }
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
