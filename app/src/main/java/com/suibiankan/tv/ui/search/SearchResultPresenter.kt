package com.suibiankan.tv.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.suibiankan.tv.R
import com.suibiankan.tv.data.remote.dto.SearchResult

/**
 * Leanback Presenter that renders a single search result as a card.
 *
 * Layout: a vertical card with a thumbnail placeholder at the top,
 * title text (2 lines), snippet text (1 line), and source domain underneath.
 */
class SearchResultPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val holder = viewHolder as SearchResultViewHolder
        val result = item as SearchResult

        holder.titleText.text = result.title
        holder.snippetText.text = result.snippet
        holder.domainText.text = result.displayUrl.ifEmpty {
            extractDomain(result.url)
        }

        // Set focus behavior
        holder.view.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start()
            } else {
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val holder = viewHolder as SearchResultViewHolder
        holder.titleText.text = ""
        holder.snippetText.text = ""
        holder.domainText.text = ""
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI.create(url)
            uri.host?.removePrefix("www.") ?: ""
        } catch (e: Exception) { "" }
    }
}

/**
 * ViewHolder for a search result card.
 */
internal class SearchResultViewHolder(view: View) : Presenter.ViewHolder(view) {
    val titleText: TextView = view.findViewById(R.id.result_title)
    val snippetText: TextView = view.findViewById(R.id.result_snippet)
    val domainText: TextView = view.findViewById(R.id.result_domain)
}
