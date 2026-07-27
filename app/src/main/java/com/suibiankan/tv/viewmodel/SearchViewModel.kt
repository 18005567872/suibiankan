package com.suibiankan.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suibiankan.tv.data.remote.SearchEngine
import com.suibiankan.tv.data.remote.dto.SearchResult
import com.suibiankan.tv.data.repository.SearchException
import com.suibiankan.tv.domain.model.SearchQuery
import com.suibiankan.tv.domain.usecase.GetSearchHistoryUseCase
import com.suibiankan.tv.domain.usecase.SearchVideosUseCase
import com.suibiankan.tv.util.ChineseTextHelper
import com.suibiankan.tv.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the search screen.
 */
class SearchViewModel(
    private val searchVideosUseCase: SearchVideosUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /** Currently selected search engine. */
    private var currentEngine = SearchEngine.DUCKDUCKGO

    init {
        loadHistory()
    }

    /**
     * Execute a search query.
     */
    fun search(query: String) {
        if (query.length < Constants.MIN_QUERY_LENGTH) return

        val normalizedQuery = ChineseTextHelper.normalizeQuery(query)
        currentEngine = ChineseTextHelper.selectEngine(normalizedQuery)

        val searchQuery = SearchQuery(
            rawQuery = query,
            normalizedQuery = normalizedQuery,
            engine = currentEngine
        )

        Timber.d("Search: \"$normalizedQuery\" via ${currentEngine.displayName}")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, query = query) }

            searchVideosUseCase(searchQuery)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = results,
                            error = null,
                            currentEngine = currentEngine
                        )
                    }
                }
                .onFailure { throwable ->
                    val errorMsg = when (throwable) {
                        is SearchException.NoResults -> throwable.message ?: "无结果"
                        is SearchException.NetworkUnavailable -> throwable.message ?: "网络不可用"
                        is SearchException.Timeout -> throwable.message ?: "超时"
                        is SearchException.RateLimited -> throwable.message ?: "太频繁"
                        else -> "搜索失败: ${throwable.message}"
                    }
                    Timber.e(throwable, "Search failed")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMsg,
                            results = emptyList()
                        )
                    }
                }
        }
    }

    /**
     * Search with a specific engine override.
     */
    fun searchWithEngine(query: String, engine: SearchEngine) {
        currentEngine = engine
        search(query)
    }

    /**
     * Switch to a different search engine and re-run the current query.
     */
    fun switchEngine(engine: SearchEngine) {
        currentEngine = engine
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            searchWithEngine(currentQuery, engine)
        }
    }

    /**
     * Clear the current search query and results.
     */
    fun clearQuery() {
        _uiState.update { SearchUiState(engine = currentEngine) }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val history = getSearchHistoryUseCase(10)
                _uiState.update {
                    it.copy(searchHistory = history.map { entity -> entity.query })
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load search history")
            }
        }
    }
}

/**
 * UI state for the search screen.
 */
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val error: String? = null,
    val searchHistory: List<String> = emptyList(),
    val currentEngine: SearchEngine = SearchEngine.DUCKDUCKGO,
    val engine: SearchEngine = SearchEngine.DUCKDUCKGO
)
