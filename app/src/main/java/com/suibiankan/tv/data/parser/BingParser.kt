package com.suibiankan.tv.data.parser

import com.suibiankan.tv.data.remote.dto.SearchResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Parses Bing HTML search results.
 *
 * Bing returns server-rendered HTML that is parsable without JavaScript,
 * making it a good fallback search engine.
 */
class BingParser : HtmlParser {

    override fun parse(html: String): List<SearchResult> {
        val doc = Jsoup.parse(html)

        // Check for CAPTCHA
        if (isRateLimited(doc)) {
            throw com.suibiankan.tv.data.repository.SearchException.RateLimited()
        }

        // Bing uses li.b_algo for main algorithmic results
        val resultElements = doc.select("li.b_algo")

        return resultElements.mapNotNull { element ->
            parseResultElement(element)
        }
    }

    private fun parseResultElement(element: org.jsoup.nodes.Element): SearchResult? {
        return try {
            // Bing uses h2 > a for the title
            val titleLink = element.selectFirst("h2 a") ?: return null
            val title = titleLink.text().trim()
            val url = titleLink.attr("href")

            // Snippet — Bing uses div.b_caption > p or multiple selectors
            val snippet = element.selectFirst("div.b_caption p, p.b_lineclamp2, p.b_lineclamp4")
                ?.text()
                ?.trim() ?: ""

            // Display URL
            val displayUrl = element.selectFirst("cite")
                ?.text()
                ?.trim() ?: extractDomain(url)

            if (title.isEmpty()) return null

            SearchResult(
                title = title,
                url = url,
                snippet = snippet,
                displayUrl = displayUrl
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI.create(url)
            uri.host?.removePrefix("www.") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun isRateLimited(doc: Document): Boolean {
        val title = doc.title().lowercase()
        return title.contains("captcha") || title.contains("verify")
    }
}
