package com.brain.words.puzzle.quotes.ui.main.game.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.databinding.MainGameTopicItemBinding
import com.brain.words.puzzle.quotes.ui.main.game.topic.section.SectionModel

class TopicAdapter : ListAdapter<SectionModel, TopicAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            R.layout.main_game_topic_item -> {
                ViewHolder(
                    MainGameTopicItemBinding.inflate(
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

    override fun getItemViewType(position: Int) = R.layout.main_game_topic_item

    data class ViewHolder(val binding: MainGameTopicItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SectionModel>() {
            override fun areItemsTheSame(
                oldItem: SectionModel, newItem: SectionModel
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: SectionModel, newItem: SectionModel
            ): Boolean = oldItem.id == newItem.id
        }
    }
}