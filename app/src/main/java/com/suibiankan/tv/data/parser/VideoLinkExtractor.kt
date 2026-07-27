package com.suibiankan.tv.data.parser

import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.data.remote.dto.VideoLinkSource
import com.suibiankan.tv.data.remote.dto.VideoQuality
import com.suibiankan.tv.util.UrlValidator
import org.jsoup.Jsoup
import timber.log.Timber

/**
 * Multi-pass video URL extraction engine.
 *
 * Strategy:
 *   Pass 1: Regex patterns for direct video file URLs (.mp4, .m3u8, etc.)
 *   Pass 2: Jsoup DOM extraction (video, source, iframe, embed tags)
 *   Pass 3: Known platform page URLs (bilibili, youku, etc.)
 *
 * Results are deduplicated by URL.
 */
class VideoLinkExtractor {

    // ──── Pass 1: Direct video URL regex patterns ────

    private val directVideoPatterns = listOf(
        // Direct video file URLs
        Regex(
            """https?://[^\s"'<>]+\.(?:mp4|mkv|avi|mov|wmv|flv|webm|ts)(?:\?[^\s"'<>]*)?(?:"|'|\s|>|$)""",
            RegexOption.IGNORE_CASE
        ),
        // HLS streams
        Regex(
            """https?://[^\s"'<>]+\.m3u8(?:\?[^\s"'<>]*)?(?:"|'|\s|>|$)""",
            RegexOption.IGNORE_CASE
        ),
    )

    private val jsVideoUrlPatterns = listOf(
        // JSON-encoded video URLs
        Regex(""""url"\s*:\s*"([^"]+\.(?:mp4|m3u8|mkv|flv|webm)[^"]*)""""),
        Regex(""""videoUrl"\s*:\s*"([^"]+)""""),
        Regex(""""src"\s*:\s*"([^"]+\.(?:mp4|m3u8|mkv|flv|webm)[^"]*)""""),
        // Var/let assignments
        Regex("""(?:var|let|const)\s+\w*\s*=\s*["']([^"']+\.(?:mp4|m3u8|mkv|flv|webm)[^"']*)["']"""),
        // data-video-url attributes
        Regex("""data-video-url\s*=\s*["']([^"']+)["']"""),
    )

    // ──── Pass 3: Known video platform URL patterns ────

    private val platformPatterns = mapOf(
        "哔哩哔哩" to listOf(
            Regex("""https?://(?:www\.)?bilibili\.com/video/(?:av|BV)[\w]+[\w/?#=&]*"""),
            Regex("""https?://(?:www\.)?b23\.tv/[\w]+"""),
        ),
        "优酷" to listOf(
            Regex("""https?://(?:www\.)?youku\.com/v_show/id_[\w=]+[\w/?#=&]*"""),
        ),
        "腾讯视频" to listOf(
            Regex("""https?://(?:www\.)?v\.qq\.com/x/(?:cover|page)/[\w]+[\w/?#=&]*"""),
        ),
        "爱奇艺" to listOf(
            Regex("""https?://(?:www\.)?iqiyi\.com/v_[\w]+[\w/?#=&]*"""),
        ),
        "芒果TV" to listOf(
            Regex("""https?://(?:www\.)?mgtv\.com/[bl]/\d+/[\w]+[\w/?#=&]*"""),
        ),
        "搜狐视频" to listOf(
            Regex("""https?://(?:www\.)?tv\.sohu\.com/\d+/[\w]+[\w/?#=&]*"""),
        ),
        "YouTube" to listOf(
            Regex("""https?://(?:www\.)?youtube\.com/watch\?v=[\w-]+"""),
            Regex("""https?://youtu\.be/[\w-]+"""),
        ),
        "Vimeo" to listOf(
            Regex("""https?://(?:www\.)?vimeo\.com/\d+"""),
        ),
    )

    // ──── Public API ────

    /**
     * Extract all video links from raw HTML content.
     *
     * @param html The raw HTML of a web page.
     * @param baseUrl The page URL (for resolving relative links).
     * @return A deduplicated list of extracted video links.
     */
    fun extract(html: String, baseUrl: String = ""): List<VideoLink> {
        val results = mutableListOf<VideoLink>()

        results.addAll(pass1DirectUrls(html))
        results.addAll(pass1JsUrlPatterns(html))
        results.addAll(pass2DomElements(html, baseUrl))
        results.addAll(pass3PlatformPages(html))

        // Deduplicate by URL
        return results.distinctBy { it.url }
    }

    // ──── Pass 1: Direct video file URLs ────

    private fun pass1DirectUrls(html: String): List<VideoLink> {
        return directVideoPatterns.flatMap { pattern ->
            pattern.findAll(html).map { match ->
                val url = UrlValidator.cleanUrl(match.value)
                if (UrlValidator.isValid(url)) {
                    VideoLink(
                        url = UrlValidator.sanitize(url),
                        source = VideoLinkSource.EXTRACTED,
                        quality = guessQuality(url)
                    )
                } else null
            }.filterNotNull()
        }
    }

    private fun pass1JsUrlPatterns(html: String): List<VideoLink> {
        return jsVideoUrlPatterns.flatMap { pattern ->
            pattern.findAll(html).mapNotNull { match ->
                val url = match.groupValues.getOrNull(1)
                    ?.let { UrlValidator.cleanUrl(it) }
                if (url != null && UrlValidator.isValid(url)) {
                    VideoLink(
                        url = UrlValidator.sanitize(url),
                        source = VideoLinkSource.EXTRACTED,
                        quality = guessQuality(url)
                    )
                } else null
            }
        }
    }

    // ──── Pass 2: DOM element extraction ────

    private fun pass2DomElements(html: String, baseUrl: String): List<VideoLink> {
        val results = mutableListOf<VideoLink>()

        val doc = Jsoup.parse(html, baseUrl)

        // <video> tag with src attribute
        doc.select("video[src]").forEach { el ->
            val src = el.attr("src")
            if (src.isNotBlank()) {
                val resolved = UrlValidator.resolve(baseUrl, src)
                results.add(
                    VideoLink(
                        url = UrlValidator.cleanUrl(resolved),
                        source = VideoLinkSource.DOM_ELEMENT,
                        quality = guessQuality(resolved)
                    )
                )
            }
        }

        // <source> tags inside <video>
        doc.select("video source[src]").forEach { el ->
            val src = el.attr("src")
            if (src.isNotBlank()) {
                val resolved = UrlValidator.resolve(baseUrl, src)
                results.add(
                    VideoLink(
                        url = UrlValidator.cleanUrl(resolved),
                        source = VideoLinkSource.DOM_ELEMENT,
                        quality = guessQuality(resolved)
                    )
                )
            }
        }

        // <iframe> tags (often embed videos)
        doc.select("iframe[src]").forEach { el ->
            val src = el.attr("src")
            if (src.isNotBlank()) {
                val resolved = UrlValidator.resolve(baseUrl, src)
                results.add(
                    VideoLink(
                        url = UrlValidator.cleanUrl(resolved),
                        source = VideoLinkSource.DOM_ELEMENT,
                        quality = VideoQuality.UNKNOWN
                    )
                )
            }
        }

        // <embed> tags
        doc.select("embed[src]").forEach { el ->
            val src = el.attr("src")
            if (src.isNotBlank()) {
                val resolved = UrlValidator.resolve(baseUrl, src)
                results.add(
                    VideoLink(
                        url = UrlValidator.cleanUrl(resolved),
                        source = VideoLinkSource.DOM_ELEMENT,
                        quality = VideoQuality.UNKNOWN
                    )
                )
            }
        }

        return results
    }

    // ──── Pass 3: Known platform page URLs ────

    private fun pass3PlatformPages(html: String): List<VideoLink> {
        return platformPatterns.flatMap { (_, patterns) ->
            patterns.flatMap { pattern ->
                pattern.findAll(html).map { match ->
                    val url = UrlValidator.cleanUrl(match.value)
                    VideoLink(
                        url = url,
                        source = VideoLinkSource.PLATFORM_PAGE,
                        quality = VideoQuality.UNKNOWN
                    )
                }
            }
        }
    }

    // ──── Quality guessing ────

    /**
     * Try to guess video quality from keywords in the URL.
     */
    fun guessQuality(url: String): VideoQuality {
        val lower = url.lowercase()
        return when {
            lower.contains("4k") || lower.contains("2160") || lower.contains("uhd") -> VideoQuality.UHD_4K
            lower.contains("1080") || lower.contains("fhd") || lower.contains("fullhd") -> VideoQuality.HD_1080
            lower.contains("720") || lower.contains("hd") -> VideoQuality.HD_720
            lower.contains("480") || lower.contains("sd") || lower.contains("360") || lower.contains("240") -> VideoQuality.SD
            else -> VideoQuality.UNKNOWN
        }
    }
}
