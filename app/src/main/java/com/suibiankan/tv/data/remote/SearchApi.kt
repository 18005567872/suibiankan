package com.suibiankan.tv.data.remote

import com.suibiankan.tv.util.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

/**
 * Retrofit service for fetching raw HTML from search engines.
 * We use @Url to dynamically switch between engine URLs.
 */
interface SearchApi {

    @GET
    suspend fun fetchSearchPage(
        @Url url: String,
        @Header("User-Agent") userAgent: String = Constants.DEFAULT_USER_AGENT
    ): Response<ResponseBody>
}
