package com.quote.mosaic.core.common.utils

import android.content.Context
import android.graphics.Rect
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Explode
import androidx.transition.Transition
import androidx.transition.TransitionManager

fun Context.findColor(color: Int) = ContextCompat.getColor(this, color)


fun ViewGroup.manageViewGroupTapable(viewGroup: ViewGroup, enabled: Boolean) {
    val childCount = viewGroup.childCount
    for (i in 0 until childCount) {
        val view = viewGroup.getChildAt(i)
        view.isEnabled = enabled
        if (view is ViewGroup) {
            manageViewGroupTapable(view, enabled)
        }
    }
}