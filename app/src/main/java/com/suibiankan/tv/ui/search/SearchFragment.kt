package com.suibiankan.tv.ui.search

import android.os.Bundle
import android.view.View
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.lifecycleScope
import com.suibiankan.tv.data.remote.dto.SearchResult
import com.suibiankan.tv.ui.detail.DetailFragment
import com.suibiankan.tv.util.NetworkUtils
import com.suibiankan.tv.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Search screen using Leanback's SearchSupportFragment.
 *
 * Shows a search orb/bar at the top and a vertical grid of results below.
 * Results update as the user types (with a minimum 2-character query).
 */
class SearchFragment : SearchSupportFragment(),
    SearchSupportFragment.SearchResultProvider {

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var resultsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSearchResultProvider(this)
        setOnItemViewClickedListener { _, item, _, _ ->
            onResultClicked(item)
        }

        // Observe ViewModel state
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                updateResults(state.results)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set search bar hint
        setSearchQuery("", false)
        // Badge drawable for the search orb
        // badgeDrawable = resources.getDrawable(R.drawable.ic_search_badge, null)
    }

    // ──── SearchResultProvider implementation ────

    override fun getResultsAdapter(): ObjectAdapter {
        resultsAdapter = ArrayObjectAdapter(SearchResultPresenter())
        return resultsAdapter
    }

    // ──── Query handling ────

    override fun onQueryTextChange(newQuery: String): Boolean {
        if (newQuery.length >= 2) {
            viewModel.search(newQuery)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (query.isNotBlank()) {
            viewModel.search(query)
        }
        return true
    }

    // ──── Result click ────

    private fun onResultClicked(item: Any?) {
        val result = item as? SearchResult ?: return
        Timber.d("Clicked: ${result.title} → ${result.url}")

        // Navigate to detail fragment
        val detailFragment = DetailFragment.newInstance(
            pageUrl = result.url,
            title = result.title
        )

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    // ──── Helpers ────

    private fun updateResults(results: List<SearchResult>) {
        if (!::resultsAdapter.isInitialized) return
        resultsAdapter.clear()
        resultsAdapter.addAll(0, results)
    }
}
