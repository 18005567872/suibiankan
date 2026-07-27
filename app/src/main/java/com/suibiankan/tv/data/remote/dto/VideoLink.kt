package com.suibiankan.tv.data.remote.dto

/**
 * An extracted video link ready for playback or further processing.
 */
data class VideoLink(
    /** The video URL (direct stream or platform page). */
    val url: String,
    /** How this link was discovered. */
    val source: VideoLinkSource,
    /** Inferred video quality. */
    val quality: VideoQuality = VideoQuality.UNKNOWN,
    /** MIME content type if known (e.g., "video/mp4"). */
    val contentType: String = ""
)

/**
 * Categorises how a video link was extracted.
 */
enum class VideoLinkSource {
    /** Direct video file URL from regex matching. */
    EXTRACTED,
    /** URL found in a DOM element (video, source, iframe, embed). */
    DOM_ELEMENT,
    /** Known video platform page (bilibili, youku, etc.). */
    PLATFORM_PAGE,
    /** Inferred from surrounding context (less reliable). */
    INFERRED
}

/**
 * Inferred video resolution quality.
 */
enum class VideoQuality(val label: String) {
    UNKNOWN("未知"),
    SD("标清"),
    HD_720("720P"),
    HD_1080("1080P"),
    UHD_4K("4K")
}
