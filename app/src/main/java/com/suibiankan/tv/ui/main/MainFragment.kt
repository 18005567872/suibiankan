package com.suibiankan.tv.ui.main

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.suibiankan.tv.R
import com.suibiankan.tv.ui.search.SearchFragment
import timber.log.Timber

/**
 * Main home screen with search entry point and history/trending rows.
 */
class MainFragment : BrowseSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainFragment created")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupSearchOrchestrator()
        loadRows()
    }

    private fun setupUI() {
        title = getString(R.string.app_name)
        // Brand color for headers
        brandColor = resources.getColor(R.color.primary, null)
        // Show a search orb at the top-right
        searchAffordanceColor = resources.getColor(R.color.search_orb, null)
    }

    private fun setupSearchOrchestrator() {
        setOnSearchClickedListener {
            // Navigate to search fragment
            val searchFragment = SearchFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadRows() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // TODO: Phase 6 — Add search history row
        // TODO: Phase 6 — Add trending searches row

        adapter = rowsAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainFragment destroyed")
    }
}
