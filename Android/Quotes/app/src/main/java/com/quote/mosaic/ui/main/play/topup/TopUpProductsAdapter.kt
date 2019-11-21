package com.quote.mosaic.ui.main.play.topup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.R
import com.quote.mosaic.databinding.TopupProductFeaturedBinding
import com.quote.mosaic.databinding.TopupProductFreeBinding
import com.quote.mosaic.databinding.TopupProductItemBinding
import com.quote.mosaic.ui.common.dialog.PaddingDividerDecoration
import com.quote.mosaic.ui.common.dialog.PaddingDividerDecoration.Companion.ORIENTATION_HORIZONTAL

class TopUpProductsAdapter(
    val onProductClicked: (TopUpProductModel) -> Unit
) : ListAdapter<TopUpProductModel, TopUpProductsAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = when (viewType) {
        R.layout.topup_product_item -> {
            ViewHolder.Item(TopupProductItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).apply {
                container.setOnClickListener { onProductClicked(item!!) }
            })
        }
        R.layout.topup_product_featured -> {
            ViewHolder.Featured(TopupProductFeaturedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).apply {
                container.setOnClickListener { onProductClicked(item!!) }
            })
        }
        R.layout.topup_product_free -> {
            ViewHolder.Free(TopupProductFreeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).apply {
                container.setOnClickListener { onProductClicked(item!!) }
            })
        }
        else -> throw IllegalArgumentException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TopUpProductModel.Item -> {
                (holder as ViewHolder.Item).binding.item = item
            }
            is TopUpProductModel.Featured -> {
                (holder as ViewHolder.Featured).binding.item = item
            }
            is TopUpProductModel.Free -> {
                (holder as ViewHolder.Free).binding.item = item
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is TopUpProductModel.Item -> R.layout.topup_product_item
        is TopUpProductModel.Featured -> R.layout.topup_product_featured
        is TopUpProductModel.Free -> R.layout.topup_product_free
    }

    sealed class ViewHolder(open val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class Item(override val binding: TopupProductItemBinding) : ViewHolder(binding)
        class Featured(override val binding: TopupProductFeaturedBinding) : ViewHolder(binding)
        class Free(override val binding: TopupProductFreeBinding) : ViewHolder(binding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TopUpProductModel>() {
            override fun areItemsTheSame(
                oldItem: TopUpProductModel, newItem: TopUpProductModel
            ): Boolean = when (oldItem) {
                is TopUpProductModel.Item -> newItem is TopUpProductModel.Item
                is TopUpProductModel.Featured -> newItem is TopUpProductModel.Featured
                is TopUpProductModel.Free -> newItem is TopUpProductModel.Free
            }

            override fun areContentsTheSame(
                oldItem: TopUpProductModel, newItem: TopUpProductModel
            ): Boolean = when (oldItem) {
                is TopUpProductModel.Item -> newItem is TopUpProductModel.Item
                is TopUpProductModel.Featured -> newItem is TopUpProductModel.Featured
                is TopUpProductModel.Free -> newItem is TopUpProductModel.Free
            }
        }

        fun decoration(padding: Int): RecyclerView.ItemDecoration = PaddingDividerDecoration(ORIENTATION_HORIZONTAL) { _, _ ->
            padding
        }
    }
}