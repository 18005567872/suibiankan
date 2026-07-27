package com.suibiankan.tv.domain.usecase

import com.suibiankan.tv.data.remote.dto.VideoLink
import com.suibiankan.tv.data.repository.SearchRepository
import timber.log.Timber

/**
 * Fetch a web page and extract video links from it.
 */
class ExtractVideoLinkUseCase(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(pageUrl: String): Result<List<VideoLink>> {
        Timber.d("ExtractVideoLinkUseCase: analyzing $pageUrl")
        return repository.extractVideoLinks(pageUrl)
    }
}
