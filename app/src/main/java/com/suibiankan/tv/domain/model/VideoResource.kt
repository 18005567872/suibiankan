package com.suibiankan.tv.domain.model

import com.suibiankan.tv.data.remote.dto.VideoLink

/**
 * Domain model representing a video resource found via search.
 */
data class VideoResource(
    /** Unique ID derived from the page URL hash. */
    val id: String,
    /** Display title. */
    val title: String,
    /** Description / snippet text. */
    val description: String,
    /** Optional thumbnail image URL. */
    val thumbnailUrl: String? = null,
    /** Original page URL from search results. */
    val pageUrl: String,
    /** Extracted video links (populated after detail extraction). */
    val videoLinks: List<VideoLink> = emptyList(),
    /** Recognised video hosting platform, if any. */
    val platform: VideoPlatform = VideoPlatform.UNKNOWN
)

/**
 * Known video hosting platforms for targeted extraction.
 */
enum class VideoPlatform(val displayName: String) {
    UNKNOWN("未知"),
    BILIBILI("哔哩哔哩"),
    YOUKU("优酷"),
    IQIYI("爱奇艺"),
    TENCENT("腾讯视频"),
    MGTV("芒果TV"),
    SOHU("搜狐视频"),
    YOUTUBE("YouTube"),
    VIMEO("Vimeo"),
    GENERIC("通用视频站")
}
