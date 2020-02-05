package com.quote.mosaic.game

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Explode
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.quote.mosaic.game.utils.AisAdapter
import com.quote.mosaic.game.utils.AisItemTouchHelperCallback

class AisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var listener: GameListener? = null

    private val mLayoutManager by lazy { FlexboxLayoutManager(context, FlexDirection.ROW) }

    private val mAdapter = AisAdapter({
        listener?.onQuoteOrderChanged(it)
    }, {
        touchHelper.startDrag(it)
    })

    private val touchHelper: ItemTouchHelper = ItemTouchHelper(AisItemTouchHelperCallback(mAdapter))

    init {
        layoutManager = mLayoutManager

        touchHelper.attachToRecyclerView(this)
    }

    fun setTextColor(color: Int) {
        mAdapter.setTextColor(color)
    }

    fun setData(mixedQuote: List<String>) {
        adapter = mAdapter
        mAdapter.setData(mixedQuote)
    }

    fun setListener(listener: GameListener) {
        this.listener = listener
    }

    fun removeItems() {
        val viewRect = Rect()
        getGlobalVisibleRect(viewRect)

        val explode: Transition = Explode().apply {
            duration = 1000
            epicenterCallback = object : Transition.EpicenterCallback() {
                override fun onGetEpicenter(transition: Transition) = viewRect
            }
        }
        TransitionManager.beginDelayedTransition(this, explode)
        adapter = null
    }
}