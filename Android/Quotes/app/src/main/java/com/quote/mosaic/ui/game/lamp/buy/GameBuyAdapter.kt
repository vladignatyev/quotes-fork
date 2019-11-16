package com.quote.mosaic.ui.game.lamp.buy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.R
import com.quote.mosaic.databinding.GameBuyPurchaseItemBinding
import com.quote.mosaic.databinding.GameBuyVideoItemBinding

class GameBuyAdapter(
    private val onSkuClicked: (GameBuyModel.PurchaseCoins) -> Unit,
    private val onRewardVideoClicked: (GameBuyModel.WatchVideo) -> Unit
) : ListAdapter<GameBuyModel, GameBuyAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = when (viewType) {
        R.layout.game_buy_purchase_item -> {
            ViewHolder.PurchaseCoins(GameBuyPurchaseItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).apply {

            })
        }
        R.layout.game_buy_video_item -> {
            ViewHolder.WatchVideo(GameBuyVideoItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).apply {

            })
        }
        else -> throw IllegalArgumentException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is GameBuyModel.PurchaseCoins -> {
                (holder as ViewHolder.PurchaseCoins).binding.item = item
            }
            is GameBuyModel.WatchVideo -> {
                (holder as ViewHolder.WatchVideo).binding.item = item
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is GameBuyModel.PurchaseCoins -> R.layout.game_buy_purchase_item
        is GameBuyModel.WatchVideo -> R.layout.game_buy_video_item
    }

    sealed class ViewHolder(open val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class PurchaseCoins(override val binding: GameBuyPurchaseItemBinding) : ViewHolder(binding)
        class WatchVideo(override val binding: GameBuyVideoItemBinding) : ViewHolder(binding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GameBuyModel>() {
            override fun areItemsTheSame(
                oldItem: GameBuyModel, newItem: GameBuyModel
            ): Boolean = when (oldItem) {
                is GameBuyModel.PurchaseCoins -> newItem is GameBuyModel.PurchaseCoins
                is GameBuyModel.WatchVideo -> newItem is GameBuyModel.WatchVideo
            }

            override fun areContentsTheSame(
                oldItem: GameBuyModel, newItem: GameBuyModel
            ): Boolean = when (oldItem) {
                is GameBuyModel.PurchaseCoins -> newItem is GameBuyModel.PurchaseCoins && oldItem.rawJson == newItem.rawJson
                is GameBuyModel.WatchVideo -> newItem is GameBuyModel.WatchVideo && oldItem.title == newItem.title
            }
        }
    }
}