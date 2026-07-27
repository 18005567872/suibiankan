package com.suibiankan.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.domain.usecase.ExtractVideoLinkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the video detail / link extraction screen.
 */
class DetailViewModel(
    private val extractVideoLinkUseCase: ExtractVideoLinkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Load a page URL and extract video links from it.
     */
    fun loadAndExtract(pageUrl: String, title: String) {
        Timber.d("DetailViewModel: loading $pageUrl")

        viewModelScope.launch {
            _uiState.update {
                it.copy(isExtracting = true, pageUrl = pageUrl, title = title, error = null)
            }

            extractVideoLinkUseCase(pageUrl)
                .onSuccess { links ->
                    Timber.d("Extracted ${links.size} video links")
                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            videoLinks = links,
                            error = if (links.isEmpty()) "未找到可播放的视频链接" else null
                        )
                    }
                }
                .onFailure { throwable ->
                    Timber.e(throwable, "Video extraction failed")
                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            error = throwable.message ?: "提取失败"
                        )
                    }
                }
        }
    }

    /**
     * Clear the current detail state.
     */
    fun clear() {
        _uiState.update { DetailUiState() }
    }
}

/**
 * UI state for the detail screen.
 */
data class DetailUiState(
    val pageUrl: String = "",
    val title: String = "",
    val isExtracting: Boolean = false,
    val videoLinks: List<VideoLink> = emptyList(),
    val error: String? = null
)
