package com.suibiankan.tv.data.parser

import com.suibiankan.tv.data.remote.dto.SearchResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLDecoder

/**
 * Parses DuckDuckGo HTML search results.
 *
 * DuckDuckGo provides a pure-HTML endpoint at html.duckduckgo.com/html/
 * which returns server-rendered results — no JavaScript required.
 */
class DuckDuckGoParser : HtmlParser {

    override fun parse(html: String): List<SearchResult> {
        val doc = Jsoup.parse(html)

        // Check for CAPTCHA or rate-limit page
        if (isRateLimited(doc)) {
            throw com.suibiankan.tv.data.repository.SearchException.RateLimited()
        }

        // Check for no-results indicator
        if (doc.select("div.no-results").isNotEmpty()) {
            return emptyList()
        }

        return doc.select("div.result").mapNotNull { element ->
            parseResultElement(element)
        }
    }

    private fun parseResultElement(element: org.jsoup.nodes.Element): SearchResult? {
        return try {
            val linkEl = element.selectFirst("a.result__a") ?: return null
            val snippetEl = element.selectFirst("a.result__snippet")
            val urlDisplayEl = element.selectFirst("span.link-text")

            val title = linkEl.text().trim()
            val rawUrl = linkEl.attr("href")
            val snippet = snippetEl?.text()?.trim() ?: ""

            if (title.isEmpty()) return null

            SearchResult(
                title = title,
                url = cleanDdgRedirect(rawUrl),
                snippet = snippet,
                displayUrl = urlDisplayEl?.text()?.trim() ?: extractDomain(cleanDdgRedirect(rawUrl))
            )
        } catch (e: Exception) {
            null // Skip malformed individual results
        }
    }

    /**
     * DuckDuckGo wraps result URLs in a redirect:
     * //duckduckgo.com/l/?uddg=https://example.com&rut=...
     * Extract the actual target URL.
     */
    private fun cleanDdgRedirect(rawUrl: String): String {
        return try {
            val cleaned = rawUrl.removePrefix("//")
            if (!cleaned.contains("uddg=")) return cleaned

            val target = cleaned
                .split("&")
                .firstOrNull { it.startsWith("uddg=") }
                ?.removePrefix("uddg=")
                ?.let { URLDecoder.decode(it, "UTF-8") }

            target ?: cleaned
        } catch (e: Exception) {
            rawUrl
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
        val bodyText = doc.body()?.text()?.lowercase() ?: ""
        val title = doc.title().lowercase()
        return title.contains("captcha") ||
            bodyText.contains("captcha") ||
            bodyText.contains("rate limit") ||
            bodyText.contains("too many requests")
    }
}
