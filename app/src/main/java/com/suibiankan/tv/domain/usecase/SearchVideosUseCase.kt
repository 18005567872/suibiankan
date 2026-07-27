package com.suibiankan.tv.domain.usecase

import com.suibiankan.tv.data.remote.dto.SearchResult
import com.suibiankan.tv.data.repository.SearchRepository
import com.suibiankan.tv.data.repository.SearchException
import com.suibiankan.tv.domain.model.SearchQuery
import timber.log.Timber

/**
 * Execute a video search across web search engines.
 */
class SearchVideosUseCase(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: SearchQuery): Result<List<SearchResult>> {
        Timber.d("SearchVideosUseCase: searching for \"${query.rawQuery}\"")
        return repository.search(query)
    }
}
