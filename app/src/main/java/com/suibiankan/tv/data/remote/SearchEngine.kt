package com.suibiankan.tv.data.remote

/**
 * Supported search engines.
 */
enum class SearchEngine(val displayName: String) {
    DUCKDUCKGO("DuckDuckGo"),
    BAIDU("百度"),
    BING("Bing");

    companion object {
        fun fromOrdinalOrDefault(ordinal: Int): SearchEngine {
            return entries.getOrElse(ordinal) { DUCKDUCKGO }
        }
    }
}
