package com.quote.mosaic.game.utils

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlin.math.abs


class AisItemTouchHelperCallback(
    private val adapter: AisTouchHelperAdapter
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = true

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ): Int {
        val dragFlags =
            ItemTouchHelper.START or
                    ItemTouchHelper.END or
                    ItemTouchHelper.UP or
                    ItemTouchHelper.DOWN

        return makeMovementFlags(
            dragFlags,
            ItemTouchHelper.ACTION_STATE_IDLE
        )
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val width = viewHolder.itemView.width.toFloat()
            val alpha = 1.0f - abs(dX) / width
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX
        } else {
            super.onChildDraw(
                c, recyclerView, viewHolder, dX, dY,
                actionState, isCurrentlyActive
            )
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {

        adapter.onItemMove(
            viewHolder.adapterPosition,
            target.adapterPosition
        )

        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int): Unit = Unit

    override fun onSelectedChanged(
        viewHolder: ViewHolder?,
        actionState: Int
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is AisItemTouchHelperViewHolder) {
                val itemViewHolder: AisItemTouchHelperViewHolder = viewHolder
                itemViewHolder.onItemSelected()
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is AisItemTouchHelperViewHolder) {
            val itemViewHolder: AisItemTouchHelperViewHolder = viewHolder
            itemViewHolder.onItemClear()
        }
    }
}