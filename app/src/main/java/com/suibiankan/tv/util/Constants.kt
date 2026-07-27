package com.suibiankan.tv.util

/**
 * App-wide constants for the 随便看 TV app.
 */
object Constants {

    /** Default User-Agent string for HTTP requests (desktop Chrome on Windows). */
    const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /** Mobile User-Agent used as fallback for some sites. */
    const val MOBILE_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    /** Connection timeout in seconds. */
    const val CONNECT_TIMEOUT_SECONDS = 15L

    /** Read timeout in seconds. */
    const val READ_TIMEOUT_SECONDS = 30L

    /** Maximum number of search retry attempts. */
    const val MAX_RETRY_ATTEMPTS = 2

    /** Base delay between retries in milliseconds. */
    const val RETRY_BASE_DELAY_MS = 1000L

    /** Maximum concurrent requests per host. */
    const val MAX_REQUESTS_PER_HOST = 3

    /** Search history to keep in local database. */
    const val MAX_SEARCH_HISTORY = 50

    /** Minimum query length to trigger search. */
    const val MIN_QUERY_LENGTH = 2
}
