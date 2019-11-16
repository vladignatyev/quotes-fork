package com.quote.mosaic.core.binding

import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter

class ConstraintLayoutBindingAdapters {

    @BindingAdapter("backgroundRes")
    fun srcBackgroundRes(view: ConstraintLayout, @DrawableRes drawableRes: Int) {
        view.background = view.context.getDrawable(drawableRes)
    }
}