package com.quote.mosaic.core.binding

import android.view.View
import androidx.annotation.DrawableRes
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

    @BindingAdapter("backgroundRes")
    fun srcBackgroundRes(view: View, @DrawableRes drawableRes: Int) {
        view.background = view.context.getDrawable(drawableRes)
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