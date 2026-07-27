package com.suibiankan.tv.data.repository

/**
 * Exception hierarchy for search-related errors.
 * Each subtype maps to a user-facing error message.
 */
sealed class SearchException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** Device has no active internet connection. */
    class NetworkUnavailable : SearchException("网络不可用，请检查网络连接")

    /** Request timed out. */
    class Timeout(cause: Throwable? = null) : SearchException("搜索超时，请重试", cause)

    /** Search engine rate-limited or CAPTCHA detected. */
    class RateLimited : SearchException("搜索太频繁，请稍后再试")

    /** Failed to parse the HTML response from the search engine. */
    class ParseError(cause: Throwable? = null) : SearchException("结果解析失败", cause)

    /** Search completed but returned no results. */
    class NoResults(query: String) : SearchException("未找到 \"$query\" 的相关结果")

    /** HTTP-level error (non-2xx status code). */
    class HttpError(code: Int) : SearchException("服务器错误 ($code)")

    /** General network I/O error. */
    class NetworkError(cause: Throwable? = null) : SearchException(
        "网络错误: ${cause?.message ?: "未知错误"}", cause
    )
}
