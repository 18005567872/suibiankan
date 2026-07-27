package com.suibiankan.tv.data.parser

import com.suibiankan.tv.data.remote.dto.SearchResult

/**
 * Strategy interface for parsing search engine HTML into [SearchResult] objects.
 * Each implementation handles one search engine's HTML structure.
 */
interface HtmlParser {

    /**
     * Parse raw HTML from a search engine results page.
     *
     * @param html The raw HTML string from the search engine.
     * @return A list of parsed search results, or empty list if no results found.
     * @throws com.suibiankan.tv.data.repository.SearchException if parsing fails.
     */
    fun parse(html: String): List<SearchResult>
}
