package com.suibiankan.tv.domain.usecase

import com.suibiankan.tv.data.local.SearchHistoryEntity
import com.suibiankan.tv.data.repository.SearchRepository

/**
 * Get recent search history from local storage.
 */
class GetSearchHistoryUseCase(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(limit: Int = 20): List<SearchHistoryEntity> {
        return repository.getHistory(limit)
    }
}
