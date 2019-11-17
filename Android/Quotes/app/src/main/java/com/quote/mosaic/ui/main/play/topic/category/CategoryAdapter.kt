package com.quote.mosaic.ui.main.play.topic.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.R
import com.quote.mosaic.databinding.CategoryClosedItemBinding
import com.quote.mosaic.databinding.CategoryCompletedItemBinding
import com.quote.mosaic.databinding.CategoryLoadingItemBinding
import com.quote.mosaic.databinding.CategoryOpenItemBinding
import com.quote.mosaic.ui.main.play.CategoryClickListener

class CategoryAdapter(
    private val listener: CategoryClickListener
) : ListAdapter<CategoryModel, CategoryAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = when (viewType) {
        R.layout.category_closed_item -> {
            ViewHolder.Closed(
                CategoryClosedItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).apply {
                    container.setOnClickListener {
                        item?.loading = true
                        notifyDataSetChanged()
                        listener.onClosedClicked(item!!.id)
                    }
                }
            )
        }
        R.layout.category_open_item -> {
            ViewHolder.Open(
                CategoryOpenItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).apply {
                    cardView.setOnClickListener {
                        listener.onOpenedClicked(item!!.id)
                    }
                }
            )
        }
        R.layout.category_completed_item -> {
            ViewHolder.Completed(
                CategoryCompletedItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).apply {
                    cardView.setOnClickListener {
                        listener.onCompletedClicked(item!!.id)
                    }
                }
            )
        }
        R.layout.category_loading_item -> {
            ViewHolder.Loading(
                CategoryLoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        else -> throw IllegalArgumentException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryModel.Closed -> {
                (holder as ViewHolder.Closed).binding.item = item
            }
            is CategoryModel.Open -> {
                (holder as ViewHolder.Open).binding.item = item
            }
            is CategoryModel.Completed -> {
                (holder as ViewHolder.Completed).binding.item = item
            }
            is CategoryModel.Loading -> {
                (holder as ViewHolder.Loading).binding.shimmerContainer.startShimmerAnimation()
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CategoryModel.Closed -> R.layout.category_closed_item
        is CategoryModel.Open -> R.layout.category_open_item
        is CategoryModel.Completed -> R.layout.category_completed_item
        is CategoryModel.Loading -> R.layout.category_loading_item
    }

    sealed class ViewHolder(open val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class Closed(override val binding: CategoryClosedItemBinding) : ViewHolder(binding)
        class Open(override val binding: CategoryOpenItemBinding) : ViewHolder(binding)
        class Completed(override val binding: CategoryCompletedItemBinding) : ViewHolder(binding)
        class Loading(override val binding: CategoryLoadingItemBinding) : ViewHolder(binding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryModel>() {
            override fun areItemsTheSame(
                oldItem: CategoryModel, newItem: CategoryModel
            ): Boolean = when (oldItem) {
                is CategoryModel.Closed -> newItem is CategoryModel.Closed
                is CategoryModel.Open -> newItem is CategoryModel.Open && oldItem.overlayResId == newItem.overlayResId
                is CategoryModel.Completed -> newItem is CategoryModel.Completed
                is CategoryModel.Loading -> newItem is CategoryModel.Loading || newItem is CategoryModel.Closed || newItem is CategoryModel.Open || newItem is CategoryModel.Completed
            }

            override fun areContentsTheSame(
                oldItem: CategoryModel, newItem: CategoryModel
            ): Boolean = when (oldItem) {
                is CategoryModel.Closed -> newItem is CategoryModel.Closed && oldItem.id == newItem.id
                is CategoryModel.Open -> newItem is CategoryModel.Open && oldItem.id == newItem.id && oldItem.overlayResId == newItem.overlayResId
                is CategoryModel.Completed -> newItem is CategoryModel.Completed && oldItem.id == newItem.id
                is CategoryModel.Loading -> newItem is CategoryModel.Loading
            }
        }
    }
}