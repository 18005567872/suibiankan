package com.suibiankan.tv.util

import java.net.URI

/**
 * Validates and sanitizes URLs extracted from web pages.
 */
object UrlValidator {

    private val validSchemes = setOf("http", "https", "rtsp", "rtmp", "mms")

    /**
     * Check if a URL string is syntactically valid and has an allowed scheme.
     */
    fun isValid(url: String): Boolean {
        return try {
            val uri = URI.create(url.trim())
            uri.scheme in validSchemes && !uri.host.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitize a URL: trim, replace spaces with %20, fix backslashes.
     */
    fun sanitize(url: String): String {
        return url
            .trim()
            .replace(" ", "%20")
            .replace("\\", "/")
    }

    /**
     * Resolve a potentially relative URL against a base URL.
     */
    fun resolve(baseUrl: String, relativeUrl: String): String {
        return try {
            URI.create(baseUrl).resolve(relativeUrl).toString()
        } catch (e: Exception) {
            relativeUrl
        }
    }

    /**
     * Clean surrounding quotes and fix protocol-relative URLs (//example.com → https://example.com).
     */
    fun cleanUrl(url: String): String {
        return url
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .let { if (it.startsWith("//")) "https:$it" else it }
    }
}
