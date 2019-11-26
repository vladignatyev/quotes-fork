package com.quote.mosaic.game.utils

import androidx.recyclerview.widget.DiffUtil

class AisDiffUtill(
    private val oldItems: List<String>,
    private val newItems: List<String>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(op: Int, np: Int): Boolean {
        return oldItems[op].length == newItems[np].length
    }

    override fun getOldListSize(): Int = oldItems.size
    override fun getNewListSize(): Int = newItems.size

    override fun areContentsTheSame(op: Int, np: Int): Boolean {
        return oldItems[op] == newItems[np]
    }
}