package com.quote.mosaic.core.common.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PaddingDividerDecoration(
    private val orientation: Int = ORIENTATION_VERTICAL,
    private val paddingDecoration: (Int, Int) -> Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(
            getDecoration(view, parent),
            getDecoration(view, parent),
            getDecoration(view, parent),
            getDecoration(view, parent)
        )
    }

    private fun getDecoration(view: View, parent: RecyclerView): Int {
        val itemPosition = parent.getChildAdapterPosition(view)

        if (itemPosition < 0) {
            return 0
        }

        val itemType = parent.adapter?.getItemViewType(itemPosition)

        val nextItemType = if (itemPosition + 1 < parent.adapter?.itemCount!!) {
            parent.adapter?.getItemViewType(itemPosition + 1)
        } else {
            RecyclerView.NO_POSITION
        }

        return paddingDecoration(itemType!!, nextItemType!!)
    }

    companion object {
        const val ORIENTATION_VERTICAL = 0
        const val ORIENTATION_HORIZONTAL = 1
    }
}