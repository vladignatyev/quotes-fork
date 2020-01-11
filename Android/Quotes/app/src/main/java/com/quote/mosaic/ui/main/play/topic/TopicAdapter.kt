package com.quote.mosaic.ui.main.play.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.R
import com.quote.mosaic.databinding.OverviewTopicErrorItemBinding
import com.quote.mosaic.databinding.OverviewTopicItemBinding
import com.quote.mosaic.databinding.OverviewTopicLoadingItemBinding
import com.quote.mosaic.ui.main.play.CategoryClickListener
import com.quote.mosaic.ui.main.play.topic.section.SectionModel

class TopicAdapter(
    private val categoryClickListener: CategoryClickListener
) : ListAdapter<SectionModel, TopicAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            R.layout.overview_topic_item -> {
                ViewHolder.Item(
                    OverviewTopicItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            R.layout.overview_topic_loading_item -> {
                ViewHolder.Loading(
                    OverviewTopicLoadingItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            R.layout.overview_topic_error_item -> {
                ViewHolder.Error(
                    OverviewTopicErrorItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ).apply {
                        refresh.setOnClickListener {
                            categoryClickListener.onRefreshClicked()
                        }
                    }
                )
            }
            else -> throw IllegalArgumentException("Unexpected view type: $viewType")
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SectionModel.Item -> {
                (holder as ViewHolder.Item).binding.item = item
                holder.binding.listener = categoryClickListener
            }
            is SectionModel.Loading -> {
                (holder as ViewHolder.Loading).binding.item = item
                holder.binding.shimmerTitle.startShimmerAnimation()
            }
            is SectionModel.Error -> {
                (holder as ViewHolder.Error).binding.item = item
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is SectionModel.Item -> R.layout.overview_topic_item
        is SectionModel.Loading -> R.layout.overview_topic_loading_item
        is SectionModel.Error -> R.layout.overview_topic_error_item
    }

    sealed class ViewHolder(open val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class Item(override val binding: OverviewTopicItemBinding) : ViewHolder(binding)
        class Loading(override val binding: OverviewTopicLoadingItemBinding) : ViewHolder(binding)
        class Error(override val binding: OverviewTopicErrorItemBinding) : ViewHolder(binding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SectionModel>() {
            override fun areItemsTheSame(
                oldItem: SectionModel, newItem: SectionModel
            ): Boolean = when (oldItem) {
                is SectionModel.Item -> newItem is SectionModel.Item
                is SectionModel.Error -> newItem is SectionModel.Error
                is SectionModel.Loading -> newItem is SectionModel.Loading
            }

            override fun areContentsTheSame(
                oldItem: SectionModel, newItem: SectionModel
            ): Boolean = when (oldItem) {
                is SectionModel.Item -> newItem is SectionModel.Item && oldItem.id == newItem.id && oldItem.categories == newItem.categories
                is SectionModel.Loading -> newItem is SectionModel.Loading
                is SectionModel.Error -> newItem is SectionModel.Error
            }
        }
    }
}