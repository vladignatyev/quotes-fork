package com.quote.mosaic.ui.game.lamp.hints

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.R
import com.quote.mosaic.databinding.GameHintUseItemBinding

class GameHintsAdapter(
    private val onHintClicked: (GameHintsModel.Hint) -> Unit
) : ListAdapter<GameHintsModel, GameHintsAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = when (viewType) {
        R.layout.game_hint_use_item -> {
            ViewHolder.Use(GameHintUseItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).apply {

            })
        }
        else -> throw IllegalArgumentException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is GameHintsModel.Hint -> {
                (holder as ViewHolder.Use).binding.item = item
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is GameHintsModel.Hint -> R.layout.game_hint_use_item
    }

    sealed class ViewHolder(open val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class Use(override val binding: GameHintUseItemBinding) : ViewHolder(binding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GameHintsModel>() {
            override fun areItemsTheSame(
                oldItem: GameHintsModel, newItem: GameHintsModel
            ): Boolean = when (oldItem) {
                is GameHintsModel.Hint -> newItem is GameHintsModel.Hint
            }

            override fun areContentsTheSame(
                oldItem: GameHintsModel, newItem: GameHintsModel
            ): Boolean = when (oldItem) {
                is GameHintsModel.Hint -> newItem is GameHintsModel.Hint && oldItem.text == newItem.text
            }
        }
    }
}