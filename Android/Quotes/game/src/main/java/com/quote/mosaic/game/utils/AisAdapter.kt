package com.quote.mosaic.game.utils

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.quote.mosaic.game.R
import kotlinx.android.extensions.LayoutContainer
import java.util.*
import kotlin.collections.ArrayList

class AisAdapter(
    private val onQuoteOrderChanged: (ArrayList<String>) -> Unit
) : RecyclerView.Adapter<AisAdapter.AisVH>(), AisTouchHelperAdapter {

    private var data: ArrayList<String> = emptyArrayList()
    private var listener: AisClickListener? = null

    private var textColor = R.color.black

    fun setTextColor(color: Int) {
        textColor = color
    }

    fun setData(mixedQuote: List<String>) {
        val result = DiffUtil.calculateDiff(AisDiffUtill(data, mixedQuote))
        data = emptyArrayList()
        data.addAll(mixedQuote)
        result.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AisVH {
        return AisVH(parent.inflate(R.layout.item_ais))
    }

    override fun onBindViewHolder(holder: AisVH, position: Int) {
        holder.bind()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class AisVH(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer, AisItemTouchHelperViewHolder {

        fun bind() {
            val model: String = data[this.adapterPosition]
            (itemView as TextView).text = model
            (itemView as TextView).setTextColor(ContextCompat.getColor(itemView.context, textColor))

            itemView.setOnClickListener {
                listener?.invoke(model)
            }
        }

        override fun onItemSelected() {
            itemView.apply {
                scaleX = SCALE_SELECT
                scaleY = SCALE_SELECT
                alpha = ALPHA_SELECT
            }
        }

        override fun onItemClear() {
            itemView.apply {
                scaleX = SCALE_CLEAR
                scaleY = SCALE_CLEAR
                alpha = ALPHA_CLEAR
            }
            onQuoteOrderChanged(data)
        }

    }

    companion object {
        const val SCALE_SELECT = 1.2f
        const val ALPHA_SELECT = 0.6f

        const val SCALE_CLEAR = 1f
        const val ALPHA_CLEAR = 1f
    }
}