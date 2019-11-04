package com.brain.words.puzzle.quotes.core.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brain.words.puzzle.quotes.ui.main.play.CategoryClickListener
import com.brain.words.puzzle.quotes.ui.main.play.topic.category.CategoryAdapter
import com.brain.words.puzzle.quotes.ui.main.play.topic.category.CategoryModel

class RecyclerViewBindingAdapters {

    @BindingAdapter("categories", "categoryClickListener", requireAll = true)
    fun categories(recyclerView: RecyclerView, quotes: List<CategoryModel>, categoryClickListener: CategoryClickListener) {
        val adapter = CategoryAdapter(categoryClickListener)
        recyclerView.adapter = adapter
        adapter.submitList(quotes)
    }
}