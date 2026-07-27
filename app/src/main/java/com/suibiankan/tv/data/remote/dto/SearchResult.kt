package com.suibiankan.tv.data.remote.dto

/**
 * Parsed search result from a search engine HTML page.
 */
data class SearchResult(
    /** Result title (clickable link text). */
    val title: String,
    /** Target page URL (cleaned from search engine redirect). */
    val url: String,
    /** Result snippet / description text. */
    val snippet: String,
    /** Short display URL shown under the title (e.g., "bilibili.com"). */
    val displayUrl: String = ""
)
