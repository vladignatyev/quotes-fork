package com.brain.words.puzzle.quotes.ui.main.game.topic.quote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.databinding.MainGameQuoteItemBinding

class QuoteAdapter : ListAdapter<QuoteModel, QuoteAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = when (viewType) {
        R.layout.main_game_quote_item -> {
            ViewHolder(
                MainGameQuoteItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        else -> throw IllegalArgumentException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.item = getItem(position)
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = R.layout.main_game_quote_item

    data class ViewHolder(val binding: MainGameQuoteItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QuoteModel>() {
            override fun areItemsTheSame(
                oldItem: QuoteModel, newItem: QuoteModel
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: QuoteModel, newItem: QuoteModel
            ): Boolean = oldItem.id == newItem.id
        }
    }
}