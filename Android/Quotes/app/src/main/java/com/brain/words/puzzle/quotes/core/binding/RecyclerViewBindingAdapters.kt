package com.brain.words.puzzle.quotes.core.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brain.words.puzzle.quotes.ui.main.game.topic.quote.QuoteAdapter
import com.brain.words.puzzle.quotes.ui.main.game.topic.quote.QuoteModel

class RecyclerViewBindingAdapters {

    @BindingAdapter("quoteItems")
    fun quoteItems(recyclerView: RecyclerView, quotes: List<QuoteModel>) {
        val adapter = QuoteAdapter()
        recyclerView.adapter = adapter
        adapter.submitList(quotes)
    }
}