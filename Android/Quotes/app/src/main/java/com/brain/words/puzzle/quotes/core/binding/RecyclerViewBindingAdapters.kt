package com.brain.words.puzzle.quotes.core.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brain.words.puzzle.quotes.generated.callback.OnClickListener
import com.brain.words.puzzle.quotes.ui.main.game.GameListener
import com.brain.words.puzzle.quotes.ui.main.game.topic.category.CategoryAdapter
import com.brain.words.puzzle.quotes.ui.main.game.topic.category.CategoryModel

class RecyclerViewBindingAdapters {

    @BindingAdapter("categories", "gameListener", requireAll = true)
    fun categories(recyclerView: RecyclerView, quotes: List<CategoryModel>, gameListener: GameListener) {
        val adapter = CategoryAdapter(gameListener)
        recyclerView.adapter = adapter
        adapter.submitList(quotes)
    }
}