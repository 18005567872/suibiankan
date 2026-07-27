package com.suibiankan.tv.util

import com.suibiankan.tv.data.remote.SearchEngine

/**
 * Utilities for Chinese (CJK) text processing and engine selection.
 */
object ChineseTextHelper {

    /**
     * Normalize full-width characters to half-width and trim whitespace.
     * Example: "流 浪 地 球" → "流浪地球"
     */
    fun normalizeQuery(input: String): String {
        return input
            .replace(Regex("""[！-～]""")) { match ->
                (match.value[0] - 0xFEE0).toChar().toString()
            }
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    /**
     * Check whether the query contains primarily CJK characters.
     */
    fun isCjkQuery(query: String): Boolean {
        if (query.isEmpty()) return false
        val cjkCount = query.count { char ->
            val block = Character.UnicodeBlock.of(char)
            block in cjkBlocks
        }
        // Consider it a CJK query if >30% of characters are CJK
        return cjkCount.toFloat() / query.length > 0.3f
    }

    /**
     * Select the best search engine based on query language.
     * CJK queries → Baidu; everything else → DuckDuckGo
     */
    fun selectEngine(query: String): SearchEngine {
        return if (isCjkQuery(query)) SearchEngine.BAIDU
        else SearchEngine.DUCKDUCKGO
    }

    private val cjkBlocks = setOf(
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
        Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
        Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
        Character.UnicodeBlock.HIRAGANA,
        Character.UnicodeBlock.KATAKANA,
        Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
    )
}
