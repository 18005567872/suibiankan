package com.suibiankan.tv.ui.webview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.suibiankan.tv.R
import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.data.remote.dto.VideoLinkSource
import com.suibiankan.tv.data.remote.dto.VideoQuality
import com.suibiankan.tv.ui.player.PlayerHelper
import com.suibiankan.tv.viewmodel.WebViewViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Fallback WebView fragment for pages where direct video links cannot be extracted.
 *
 * Features:
 *   - Loads the target page in a TV-optimized WebView
 *   - Injects JavaScript to detect <video> elements after page load
 *   - Shows a "Play detected video" prompt if a video is found
 *   - Supports DPAD navigation for scrolling
 */
class WebViewFragment : Fragment() {

    private val viewModel: WebViewViewModel by viewModel()

    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null
    private var url: String = ""

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String): WebViewFragment {
            return WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(ARG_URL) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.webview_progress)
        val webViewContainer = view.findViewById<FrameLayout>(R.id.webview_container)

        webView = WebView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setupWebView(this)
        }

        webViewContainer?.addView(webView)

        // Start loading
        webView?.loadUrl(url)
    }

    private fun setupWebView(wv: WebView) {
        with(wv.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            // TV remote-friendly: support zoom
            builtInZoomControls = false
            displayZoomControls = false
            // Security
            allowFileAccess = false
            allowContentAccess = false
        }

        wv.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val requestUrl = request?.url?.toString() ?: return false

                // If the URL looks like a direct video, try to play it
                if (isVideoUrl(requestUrl)) {
                    val videoLink = VideoLink(
                        url = requestUrl,
                        source = VideoLinkSource.EXTRACTED,
                        quality = VideoQuality.UNKNOWN
                    )
                    PlayerHelper.play(requireContext(), videoLink)
                    return true
                }

                return false
            }

            override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                super.onPageFinished(view, loadedUrl)
                viewModel.onPageLoaded()
                progressBar?.visibility = View.GONE

                // Inject JavaScript to detect video elements
                injectVideoDetector(view)
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.progress = newProgress
                }
            }
        }
    }

    /**
     * Inject JavaScript that scans the page for <video> elements
     * and reports detected video URLs back to us.
     */
    private fun injectVideoDetector(view: WebView?) {
        val js = """
            (function() {
                var videos = document.querySelectorAll('video');
                var results = [];
                videos.forEach(function(v, idx) {
                    var src = v.currentSrc || v.src;
                    var sources = v.querySelectorAll('source');
                    if (!src && sources.length > 0) {
                        src = sources[0].src;
                    }
                    if (src) {
                        results.push(src);
                    }
                });
                // Also check for known video player containers
                var iframes = document.querySelectorAll('iframe[src*="player"], iframe[src*="video"]');
                iframes.forEach(function(f) {
                    results.push('IFRAME:' + f.src);
                });
                JSON.stringify(results);
            })();
        """.trimIndent()

        view?.evaluateJavascript(js) { result ->
            if (result != null && result != "null" && result != "[]") {
                try {
                    val urls = result
                        .removeSurrounding("\"")
                        .removeSurrounding("[")
                        .removeSurrounding("]")
                        .split(",")
                        .map { it.removeSurrounding("\"").trim() }
                        .filter { it.isNotBlank() }

                    if (urls.isNotEmpty()) {
                        val videoUrl = urls.first().removePrefix("IFRAME:")
                        viewModel.onVideoDetected(videoUrl)
                        Timber.d("WebView detected video: $videoUrl")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse video detector result")
                }
            }
        }
    }

    /**
     * Handle DPAD keys for scrolling the WebView.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        // Let WebView handle DPAD for scrolling
        return webView?.dispatchKeyEvent(event) ?: false
    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(
            ".mp4", ".m3u8", ".mkv", ".avi", ".mov", ".wmv", ".flv",
            ".webm", ".ts", ".mpd", ".ogv", ".3gp"
        )
        return videoExtensions.any { ext ->
            url.lowercase().contains(ext)
        }
    }

    override fun onDestroyView() {
        webView?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            destroy()
        }
        webView = null
        super.onDestroyView()
    }
}
