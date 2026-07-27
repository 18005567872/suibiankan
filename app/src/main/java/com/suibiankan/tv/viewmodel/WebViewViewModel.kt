package com.suibiankan.tv.viewmodel

import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the WebView fallback screen.
 */
class WebViewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WebViewUiState())
    val uiState: StateFlow<WebViewUiState> = _uiState.asStateFlow()

    /**
     * Start loading a URL in the WebView.
     */
    fun loadUrl(url: String) {
        val sanitized = if (URLUtil.isValidUrl(url)) url
        else "https://$url"
        _uiState.update {
            it.copy(url = sanitized, isLoading = true, detectedVideoUrl = null)
        }
    }

    /**
     * Called when the page has finished loading.
     */
    fun onPageLoaded() {
        _uiState.update { it.copy(isLoading = false) }
    }

    /**
     * Called when a video element is detected via JavaScript injection.
     */
    fun onVideoDetected(videoUrl: String) {
        _uiState.update { it.copy(detectedVideoUrl = videoUrl) }
    }

    /**
     * Clear any detected video URL.
     */
    fun clearDetectedVideo() {
        _uiState.update { it.copy(detectedVideoUrl = null) }
    }
}

/**
 * UI state for the WebView screen.
 */
data class WebViewUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val detectedVideoUrl: String? = null
)
