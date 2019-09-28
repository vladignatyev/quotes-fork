package com.brain.words.puzzle.quotes.core.binding

import android.view.View
import androidx.databinding.BindingAdapter

class ViewBindingAdapters {

    @BindingAdapter("visible")
    fun setVisible(view: View, visible: Boolean) {
        view.setVisible(visible)
    }

    @BindingAdapter("invisible")
    fun setInvisible(view: View, invisible: Boolean) {
        view.setInvisible(invisible)
    }

}

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.setInvisible(invisible: Boolean) {
    visibility = if (invisible) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}