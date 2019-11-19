package com.quote.mosaic.ui.game.hint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.R
import com.quote.mosaic.databinding.HintBalanceItemBinding
import com.quote.mosaic.databinding.HintCloseItemBinding
import com.quote.mosaic.databinding.HintCoinItemBinding
import com.quote.mosaic.databinding.HintSkipItemBinding

class HintAdapter(
    private val onCloseClicked: () -> Unit,
    private val onBalanceClicked: () -> Unit,
    private val onHintClicked: (HintModel) -> Unit
) : ListAdapter<HintModel, HintAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = when (viewType) {
        R.layout.hint_balance_item -> {
            ViewHolder.Balance(
                HintBalanceItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).apply {
                    topup.setOnClickListener { onBalanceClicked() }
                })
        }
        R.layout.hint_close_item -> {
            ViewHolder.Close(
                HintCloseItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).apply {
                    close.setOnClickListener { onCloseClicked() }
                })
        }
        R.layout.hint_coin_item -> {
            ViewHolder.SkipHint(
                HintSkipItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).apply {
                    container.setOnClickListener { onHintClicked(item!!) }
                })
        }
        R.layout.hint_skip_item -> {
            ViewHolder.CoinHint(
                HintCoinItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).apply {
                    container.setOnClickListener { onHintClicked(item!!) }
                })
        }
        else -> throw IllegalArgumentException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HintModel.Balance -> {
                (holder as ViewHolder.Balance).binding.item = item
            }
            is HintModel.Close -> {
                (holder as ViewHolder.Close).binding.item = item
            }
            is HintModel.SkipHint -> {
                (holder as ViewHolder.SkipHint).binding.item = item
            }
            is HintModel.CoinHint -> {
                (holder as ViewHolder.CoinHint).binding.item = item
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HintModel.Balance -> R.layout.hint_balance_item
        is HintModel.Close -> R.layout.hint_close_item
        is HintModel.SkipHint -> R.layout.hint_coin_item
        is HintModel.CoinHint -> R.layout.hint_skip_item
    }

    sealed class ViewHolder(open val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class Balance(override val binding: HintBalanceItemBinding) : ViewHolder(binding)
        class Close(override val binding: HintCloseItemBinding) : ViewHolder(binding)
        class SkipHint(override val binding: HintSkipItemBinding) : ViewHolder(binding)
        class CoinHint(override val binding: HintCoinItemBinding) : ViewHolder(binding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HintModel>() {
            override fun areItemsTheSame(
                oldItem: HintModel, newItem: HintModel
            ): Boolean = when (oldItem) {
                is HintModel.SkipHint -> newItem is HintModel.SkipHint
                is HintModel.CoinHint -> newItem is HintModel.CoinHint
                is HintModel.Close -> newItem is HintModel.Close
                is HintModel.Balance -> newItem is HintModel.Balance
            }

            override fun areContentsTheSame(
                oldItem: HintModel, newItem: HintModel
            ): Boolean = when (oldItem) {
                is HintModel.Close -> newItem is HintModel.Close
                is HintModel.Balance -> newItem is HintModel.Balance && oldItem.balance == newItem.balance
                is HintModel.SkipHint -> newItem is HintModel.SkipHint && oldItem.text == newItem.text
                is HintModel.CoinHint -> newItem is HintModel.CoinHint && oldItem.text == newItem.text
            }
        }
    }
}