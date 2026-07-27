package com.suibiankan.tv.data.repository

import com.suibiankan.tv.data.local.SearchHistoryDao
import com.suibiankan.tv.data.local.SearchHistoryEntity
import com.suibiankan.tv.data.parser.HtmlParser
import com.suibiankan.tv.data.parser.VideoLinkExtractor
import com.suibiankan.tv.data.remote.SearchApi
import com.suibiankan.tv.data.remote.SearchEngine
import com.suibiankan.tv.data.remote.dto.SearchResult
import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.domain.model.SearchQuery
import com.suibiankan.tv.util.Constants
import kotlinx.coroutines.delay
import timber.log.Timber
import java.net.URLEncoder

/**
 * Concrete implementation of [SearchRepository].
 */
class SearchRepositoryImpl(
    private val api: SearchApi,
    private val parsers: Map<SearchEngine, HtmlParser>,
    private val videoLinkExtractor: VideoLinkExtractor,
    private val searchHistoryDao: SearchHistoryDao
) : SearchRepository {

    // ──── Search ────

    override suspend fun search(query: SearchQuery): Result<List<SearchResult>> {
        val engineUrl = buildSearchUrl(query)
        Timber.d("Searching ${query.engine.displayName}: ${query.normalizedQuery}")

        return withRetry {
            val response = api.fetchSearchPage(engineUrl)

            if (!response.isSuccessful) {
                return@withRetry Result.failure(
                    SearchException.HttpError(response.code())
                )
            }

            val html = response.body()?.string() ?: ""
            if (html.isBlank()) {
                return@withRetry Result.failure(
                    SearchException.NoResults(query.rawQuery)
                )
            }

            val parser = parsers[query.engine]
                ?: return@withRetry Result.failure(
                    SearchException.ParseError(IllegalStateException("No parser for ${query.engine}"))
                )

            val results = parser.parse(html)

            if (results.isEmpty()) {
                return@withRetry Result.failure(
                    SearchException.NoResults(query.rawQuery)
                )
            }

            Result.success(results)
        }
    }

    // ──── Video Link Extraction ────

    override suspend fun extractVideoLinks(pageUrl: String): Result<List<VideoLink>> {
        Timber.d("Extracting video links from: $pageUrl")

        return withRetry {
            val response = api.fetchSearchPage(pageUrl)

            if (!response.isSuccessful) {
                return@withRetry Result.failure(
                    SearchException.HttpError(response.code())
                )
            }

            val html = response.body()?.string() ?: ""
            if (html.isBlank()) {
                return@withRetry Result.success(emptyList())
            }

            val links = videoLinkExtractor.extract(html, pageUrl)
            Timber.d("Found ${links.size} video links on $pageUrl")
            Result.success(links)
        }
    }

    // ──── Search History ────

    override suspend fun getHistory(limit: Int): List<SearchHistoryEntity> {
        return searchHistoryDao.getRecent(limit)
    }

    override suspend fun saveToHistory(query: String) {
        searchHistoryDao.insert(SearchHistoryEntity(query = query))
        // Trim old entries beyond the limit
        searchHistoryDao.deleteOldest(Constants.MAX_SEARCH_HISTORY)
    }

    override suspend fun clearHistory() {
        searchHistoryDao.deleteAll()
    }

    // ──── Private Helpers ────

    /**
     * Build the search URL for a given query and engine.
     */
    private fun buildSearchUrl(query: SearchQuery): String {
        val encoded = URLEncoder.encode(query.normalizedQuery, "UTF-8")
        return when (query.engine) {
            SearchEngine.DUCKDUCKGO ->
                "https://html.duckduckgo.com/html/?q=$encoded"
            SearchEngine.BAIDU -> {
                val offset = query.page * 10
                "https://www.baidu.com/s?wd=$encoded&pn=$offset"
            }
            SearchEngine.BING -> {
                val offset = query.page * 10 + 1
                "https://www.bing.com/search?q=$encoded&first=$offset"
            }
        }
    }

    /**
     * Retry the block up to [Constants.MAX_RETRY_ATTEMPTS] times with linear backoff.
     */
    private suspend fun <T> withRetry(
        maxAttempts: Int = Constants.MAX_RETRY_ATTEMPTS,
        block: suspend () -> Result<T>
    ): Result<T> {
        var lastError: Result<T>? = null
        repeat(maxAttempts) { attempt ->
            try {
                val result = block()
                if (result.isSuccess) return result
                // Don't retry on client errors like rate-limiting or no-results
                val exception = result.exceptionOrNull()
                if (exception is SearchException.RateLimited ||
                    exception is SearchException.NoResults) {
                    return result
                }
                lastError = result
            } catch (e: java.io.IOException) {
                lastError = Result.failure(SearchException.NetworkError(e))
            }

            if (attempt < maxAttempts - 1) {
                delay(Constants.RETRY_BASE_DELAY_MS * (attempt + 1))
            }
        }
        return lastError ?: Result.failure(SearchException.NetworkError())
    }
}
