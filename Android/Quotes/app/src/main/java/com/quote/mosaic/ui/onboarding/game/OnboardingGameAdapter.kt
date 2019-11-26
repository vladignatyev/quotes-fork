package com.quote.mosaic.ui.onboarding.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.game.draggable.DraggableItemAdapter
import com.quote.mosaic.game.draggable.DraggableItemConstants
import com.quote.mosaic.game.draggable.ItemDraggableRange
import com.quote.mosaic.game.utils.AbstractDraggableItemViewHolder
import com.quote.mosaic.R
import com.quote.mosaic.core.ui.DrawableUtils
import com.quote.mosaic.core.ui.data.AbstractDataProvider
import kotlinx.android.synthetic.main.game_item.view.*

class OnboardingGameAdapter(
    private val textColorResId: Int,
    private val onSuccess: () -> Unit
) : RecyclerView.Adapter<OnboardingGameAdapter.NormalItemViewHolder>(),
    DraggableItemAdapter<OnboardingGameAdapter.NormalItemViewHolder> {

    private lateinit var dataProvider: AbstractDataProvider

    init {
        // DraggableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true)
    }

    fun setDataProvider(dataProvider: AbstractDataProvider) {
        this.dataProvider = dataProvider
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int) = dataProvider.getItem(position).id

    override fun getItemViewType(position: Int) = dataProvider.getItem(position).viewType + 1

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = NormalItemViewHolder(LayoutInflater.from(parent.context).inflate(
        R.layout.game_item, parent, false
    ).apply {
        word.setTextColor(ContextCompat.getColor(context, textColorResId))
    })

    override fun onBindViewHolder(holder: NormalItemViewHolder, position: Int) {
        val item = dataProvider.getItem(position)

        // set text
        holder.word.text = item.text

        // set background resource (target view ID: container)
        val dragState = holder.dragStateFlags

        if (dragState and DraggableItemConstants.STATE_FLAG_IS_UPDATED != 0) {

            val bgResId = when {
                dragState and DraggableItemConstants.STATE_FLAG_IS_ACTIVE != 0 -> {
                    DrawableUtils.clearState(holder.container.foreground)
                    R.drawable.bg_item_dragging_active_state
                }
                dragState and DraggableItemConstants.STATE_FLAG_DRAGGING != 0 -> R.drawable.bg_item_dragging_state
                else -> R.drawable.bg_item_normal_state
            }

            holder.container.setBackgroundResource(bgResId)
        }
    }

    override fun getItemCount() = dataProvider.count

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        dataProvider.moveItem(fromPosition, toPosition)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int) = true

    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
        val userQuote = dataProvider.getCurrentQuote().joinToString(" ").replace("\u200E","")
        val correctQuote = dataProvider.getFullQuote().joinToString(" ").replace("\u200E","")

        if (userQuote.equals(correctQuote)) {
            onSuccess()
        }
    }

    override fun onCheckCanStartDrag(
        holder: NormalItemViewHolder, position: Int, x: Int, y: Int
    ) = true

    override fun onGetItemDraggableRange(
        holder: NormalItemViewHolder,
        position: Int
    ) = ItemDraggableRange(0, itemCount - 1)

    class NormalItemViewHolder(v: View) : AbstractDraggableItemViewHolder(v) {
        var container: FrameLayout = v.findViewById(R.id.container)
        var word: TextView = v.findViewById(R.id.word)
    }
}