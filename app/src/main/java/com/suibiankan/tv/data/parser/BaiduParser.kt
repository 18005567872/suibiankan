package com.suibiankan.tv.data.parser

import com.suibiankan.tv.data.remote.dto.SearchResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Parses Baidu (百度) HTML search results.
 *
 * Baidu's HTML structure uses server-rendered pages with consistent selectors.
 * This parser is optimised for Chinese-language queries.
 */
class BaiduParser : HtmlParser {

    override fun parse(html: String): List<SearchResult> {
        val doc = Jsoup.parse(html)

        // Check for CAPTCHA
        if (isRateLimited(doc)) {
            throw com.suibiankan.tv.data.repository.SearchException.RateLimited()
        }

        // Baidu results are in div.result or div.result-op containers
        val resultElements = doc.select("div.result, div.result-op")

        return resultElements.mapNotNull { element ->
            parseResultElement(element)
        }
    }

    private fun parseResultElement(element: org.jsoup.nodes.Element): SearchResult? {
        return try {
            // Baidu uses h3 > a for the title link
            val titleLink = element.selectFirst("h3 a") ?: return null
            val title = titleLink.text().trim()

            // Baidu sometimes wraps URLs — look for data-url or the raw href
            val url = titleLink.attr("data-url")
                .ifEmpty { titleLink.attr("href") }

            // Snippet text
            val snippet = element.selectFirst("span.content-right_8Zs40, div.c-abstract, span.c-font-normal")
                ?.text()
                ?.trim() ?: ""

            // Display URL
            val displayUrl = element.selectFirst("span.c-showurl, a.c-showurl")
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
        val bodyText = doc.body()?.text()?.lowercase() ?: ""
        return title.contains("验证") ||
            title.contains("captcha") ||
            bodyText.contains("请输入验证码") ||
            bodyText.contains("安全验证") ||
            bodyText.contains("访问限制")
    }
}
