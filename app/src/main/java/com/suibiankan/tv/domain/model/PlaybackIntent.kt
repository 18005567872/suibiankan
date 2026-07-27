package com.suibiankan.tv.domain.model

import android.content.Intent
import com.suibiankan.tv.data.remote.dto.VideoLink

/**
 * Wraps a playback [Intent] with metadata about the video.
 */
data class PlaybackIntent(
    /** The Android Intent ready to start for playback. */
    val intent: Intent,
    /** The source video link. */
    val videoLink: VideoLink,
    /** Human-readable title for the video. */
    val title: String
)
