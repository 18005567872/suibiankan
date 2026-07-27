package com.suibiankan.tv.ui.player

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.data.remote.dto.VideoLinkSource
import timber.log.Timber

/**
 * Builds and launches playback Intents for video links.
 *
 * Strategy (in priority order):
 *   1. Direct video file URL → ACTION_VIEW with MIME type → system media player
 *   2. Platform page URL → ACTION_VIEW without MIME → deep-link to platform app
 *   3. Fallback → raw ACTION_VIEW → let Android figure it out
 */
object PlayerHelper {

    /**
     * Try to play a video link.
     *
     * @return true if an Intent was successfully launched, false if no handler was found.
     */
    fun play(context: Context, videoLink: VideoLink): Boolean {
        val intent = buildIntent(videoLink)
        return try {
            val packageManager = context.packageManager

            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // No direct handler — try raw ACTION_VIEW as last resort
                Timber.d("No direct handler for ${videoLink.url}, trying fallback")
                val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(videoLink.url))
                if (fallback.resolveActivity(packageManager) != null) {
                    context.startActivity(fallback)
                    true
                } else {
                    Timber.e("No handler at all for ${videoLink.url}")
                    false
                }
            }
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Activity not found for ${videoLink.url}")
            false
        }
    }

    /**
     * Build an appropriate Intent for the given video link.
     */
    fun buildIntent(videoLink: VideoLink): Intent {
        val uri = Uri.parse(videoLink.url)

        return when (videoLink.source) {
            VideoLinkSource.EXTRACTED,
            VideoLinkSource.DOM_ELEMENT -> {
                // Direct video file — set MIME type so system picks a video player
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, inferMimeType(videoLink))
                    putExtra("title", videoLink.url.substringAfterLast("/"))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            VideoLinkSource.PLATFORM_PAGE -> {
                // Platform page — no MIME type, hope for deep-link to platform app
                Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            VideoLinkSource.INFERRED -> {
                // Last resort: raw view
                Intent(Intent.ACTION_VIEW, uri)
            }
        }
    }

    /**
     * Infer the MIME type from the video URL's file extension.
     */
    private fun inferMimeType(videoLink: VideoLink): String {
        if (videoLink.contentType.isNotBlank()) {
            return videoLink.contentType
        }
        val ext = videoLink.url
            .substringAfterLast('.')
            .substringBefore('?')
            .lowercase()
        return when (ext) {
            "mp4"  -> "video/mp4"
            "mkv"  -> "video/x-matroska"
            "webm" -> "video/webm"
            "flv"  -> "video/x-flv"
            "avi"  -> "video/x-msvideo"
            "mov"  -> "video/quicktime"
            "wmv"  -> "video/x-ms-wmv"
            "m3u8" -> "application/x-mpegURL"
            "ts"   -> "video/mp2t"
            "mpd"  -> "application/dash+xml"
            else   -> "video/*"
        }
    }
}
