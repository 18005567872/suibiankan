package com.suibiankan.tv.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.suibiankan.tv.R
import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.data.remote.dto.VideoLinkSource
import com.suibiankan.tv.ui.player.PlayerHelper
import com.suibiankan.tv.ui.webview.WebViewFragment
import com.suibiankan.tv.viewmodel.DetailViewModel
import com.suibiankan.tv.viewmodel.DetailUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Detail screen showing a video page's extracted links and playback options.
 *
 * Uses Leanback's DetailsSupportFragment for the TV-optimized detail layout.
 * Contains:
 *   - Overview row with title and extraction status
 *   - Actions row with "Play" and fallback options
 *   - Video links list (if multiple links found)
 */
class DetailFragment : DetailsSupportFragment() {

    private val viewModel: DetailViewModel by viewModel()

    private lateinit var pageUrl: String
    private lateinit var pageTitle: String

    companion object {
        private const val ARG_PAGE_URL = "page_url"
        private const val ARG_TITLE = "title"

        // Action IDs
        const val ACTION_PLAY_DIRECT = 1L
        const val ACTION_PLAY_PLATFORM = 2L
        const val ACTION_WEBVIEW = 3L
        const val ACTION_BROWSER = 4L

        fun newInstance(pageUrl: String, title: String): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PAGE_URL, pageUrl)
                    putString(ARG_TITLE, title)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageUrl = arguments?.getString(ARG_PAGE_URL) ?: ""
        pageTitle = arguments?.getString(ARG_TITLE) ?: ""

        // Register item click listener once
        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is DetailAction -> handleAction(item)
                is VideoLink -> {
                    if (!PlayerHelper.play(requireContext(), item)) {
                        Toast.makeText(context, R.string.no_player, Toast.LENGTH_LONG).show()
                        openInWebView(pageUrl)
                    }
                }
            }
        }

        // Start video extraction
        viewModel.loadAndExtract(pageUrl, pageTitle)

        // Observe state
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                setupDetailRows(state)
            }
        }
    }

    private fun setupDetailRows(state: DetailUiState) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // ── Overview Row: title + status ──
        rowsAdapter.add(buildOverviewRow(state))

        // ── Actions Row ──
        rowsAdapter.add(buildActionsRow(state))

        // ── Video Links Row (if any) ──
        if (state.videoLinks.isNotEmpty()) {
            rowsAdapter.add(buildVideoLinksRow(state.videoLinks))
        }

        adapter = rowsAdapter
    }

    private fun buildOverviewRow(state: DetailUiState): ListRow {
        val header = HeaderItem(0, state.title)
        val statusText = when {
            state.isExtracting -> "正在提取视频链接…"
            state.videoLinks.isEmpty() -> state.error ?: "未找到可播放的视频链接"
            else -> "找到 ${state.videoLinks.size} 个视频链接"
        }
        val adapter = ArrayObjectAdapter(TextPresenter())
        adapter.add(statusText)
        return ListRow(header, adapter)
    }

    private fun buildActionsRow(state: DetailUiState): ListRow {
        val header = HeaderItem(1, "操作")
        val actionsAdapter = ArrayObjectAdapter(DetailActionPresenter())

        val directLinks = state.videoLinks.filter {
            it.source == VideoLinkSource.EXTRACTED || it.source == VideoLinkSource.DOM_ELEMENT
        }
        val platformLinks = state.videoLinks.filter {
            it.source == VideoLinkSource.PLATFORM_PAGE
        }

        // Action 1: Play direct video if available
        if (directLinks.isNotEmpty()) {
            val firstLink = directLinks.first()
            actionsAdapter.add(
                DetailAction(
                    id = ACTION_PLAY_DIRECT,
                    label = "播放视频",
                    description = "${firstLink.quality.label} ${extractFileName(firstLink.url)}"
                )
            )
        } else if (platformLinks.isNotEmpty()) {
            actionsAdapter.add(
                DetailAction(
                    id = ACTION_PLAY_PLATFORM,
                    label = "打开平台播放",
                    description = "尝试通过系统 App 打开"
                )
            )
        }

        // Action 2: Open in WebView
        actionsAdapter.add(
            DetailAction(
                id = ACTION_WEBVIEW,
                label = "在 WebView 中打开",
                description = "内置浏览器中浏览页面"
            )
        )

        // Action 3: Open in system browser
        actionsAdapter.add(
            DetailAction(
                id = ACTION_BROWSER,
                label = "系统浏览器打开",
                description = "通过系统浏览器打开"
            )
        )

        return ListRow(header, actionsAdapter)
    }

    private fun buildVideoLinksRow(links: List<VideoLink>): ListRow {
        val header = HeaderItem(2, "视频链接 (${links.size})")
        val adapter = ArrayObjectAdapter(VideoLinkPresenter())
        adapter.addAll(0, links)
        return ListRow(header, adapter)
    }

    // ──── Action Handling ────

    private fun handleAction(action: DetailAction) {
        when (action.id) {
            ACTION_PLAY_DIRECT, ACTION_PLAY_PLATFORM -> {
                val state = viewModel.uiState.value
                val directLinks = state.videoLinks.filter {
                    it.source == VideoLinkSource.EXTRACTED || it.source == VideoLinkSource.DOM_ELEMENT
                }
                val platformLinks = state.videoLinks.filter {
                    it.source == VideoLinkSource.PLATFORM_PAGE
                }
                val linkToPlay = directLinks.firstOrNull() ?: platformLinks.firstOrNull()
                if (linkToPlay != null) {
                    if (!PlayerHelper.play(requireContext(), linkToPlay)) {
                        Toast.makeText(context, R.string.no_player, Toast.LENGTH_LONG).show()
                        openInWebView(pageUrl)
                    }
                } else {
                    openInWebView(pageUrl)
                }
            }
            ACTION_WEBVIEW -> openInWebView(pageUrl)
            ACTION_BROWSER -> openInSystemBrowser(pageUrl)
        }
    }

    private fun openInWebView(url: String) {
        val webViewFragment = WebViewFragment.newInstance(url)
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, webViewFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openInSystemBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractFileName(url: String): String {
        return url.substringAfterLast("/").substringBefore("?").take(30)
    }

}

// ──── Detail-specific Data Classes ────

data class DetailAction(
    val id: Long,
    val label: String,
    val description: String = ""
)

// ──── Leanback Presenters ────

/** Simple text presenter for the overview/status row. */
class TextPresenter : Presenter() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
        val view = TextView(parent.context).apply {
            textSize = 16f
            setTextColor(parent.context.resources.getColor(R.color.text_secondary, null))
            setPadding(48, 16, 48, 24)
            maxLines = 3
        }
        return object : ViewHolder(view) {}
    }

    override fun onBindViewHolder(vh: ViewHolder, item: Any) {
        (vh.view as TextView).text = item as String
    }

    override fun onUnbindViewHolder(vh: ViewHolder) {
        (vh.view as TextView).text = ""
    }
}

/** Presenter for action items (renders as clickable text rows). */
class DetailActionPresenter : Presenter() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
        val view = TextView(parent.context).apply {
            textSize = 15f
            setTextColor(parent.context.resources.getColor(R.color.text_primary, null))
            setPadding(48, 12, 48, 12)
            isFocusable = true
            isClickable = true
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start()
                    (v as TextView).setTextColor(
                        v.context.resources.getColor(R.color.primary, null)
                    )
                } else {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    (v as TextView).setTextColor(
                        v.context.resources.getColor(R.color.text_primary, null)
                    )
                }
            }
        }
        return object : ViewHolder(view) {}
    }

    override fun onBindViewHolder(vh: ViewHolder, item: Any) {
        val action = item as DetailAction
        (vh.view as TextView).text = "${action.label}  —  ${action.description}"
    }

    override fun onUnbindViewHolder(vh: ViewHolder) {
        (vh.view as TextView).text = ""
    }
}

/** Presenter for video link items. */
class VideoLinkPresenter : Presenter() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
        val view = TextView(parent.context).apply {
            textSize = 13f
            setTextColor(parent.context.resources.getColor(R.color.text_secondary, null))
            setPadding(48, 8, 48, 8)
            isFocusable = true
            isClickable = true
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start()
                } else {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }
            }
        }
        return object : ViewHolder(view) {}
    }

    override fun onBindViewHolder(vh: ViewHolder, item: Any) {
        val link = item as VideoLink
        val displayUrl = link.url.let { url ->
            if (url.length > 60) url.take(57) + "..." else url
        }
        (vh.view as TextView).text = "[${link.quality.label}] $displayUrl"
    }

    override fun onUnbindViewHolder(vh: ViewHolder) {
        (vh.view as TextView).text = ""
    }
}
