package com.suibiankan.tv.data.repository

import com.suibiankan.tv.data.local.SearchHistoryEntity
import com.suibiankan.tv.data.remote.dto.SearchResult
import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.domain.model.SearchQuery

/**
 * Central repository for search and video extraction operations.
 */
interface SearchRepository {

    /**
     * Execute a web search and return parsed results.
     *
     * @param query The search query with engine selection.
     * @return [Result] with list of [SearchResult] on success, or a [SearchException] on failure.
     */
    suspend fun search(query: SearchQuery): Result<List<SearchResult>>

    /**
     * Fetch a web page and extract video links from it.
     *
     * @param pageUrl The URL of the page to analyze.
     * @return [Result] with list of [VideoLink] on success.
     */
    suspend fun extractVideoLinks(pageUrl: String): Result<List<VideoLink>>

    /**
     * Get recent search history from the local database.
     */
    suspend fun getHistory(limit: Int = 20): List<SearchHistoryEntity>

    /**
     * Save a search query to local history.
     */
    suspend fun saveToHistory(query: String)

    /**
     * Clear all search history.
     */
    suspend fun clearHistory()
}
