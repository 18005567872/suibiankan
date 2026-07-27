package com.suibiankan.tv.domain.model

import com.suibiankan.tv.data.remote.SearchEngine

/**
 * Wrapper for a search query with metadata for engine selection.
 */
data class SearchQuery(
    /** Original user-typed query string. */
    val rawQuery: String,
    /** Normalized query string (full-width → half-width, trimmed). */
    val normalizedQuery: String,
    /** Which search engine to use. */
    val engine: SearchEngine,
    /** Page offset (0-based, for pagination). */
    val page: Int = 0
)
