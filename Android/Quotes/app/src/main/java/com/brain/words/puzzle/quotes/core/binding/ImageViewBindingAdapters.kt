package com.brain.words.puzzle.quotes.core.binding

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter

class ImageViewBindingAdapters {

    @BindingAdapter("srcCompatRes")
    fun srcCompatRes(view: ImageView, @DrawableRes drawableRes: Int) {
        view.setImageResource(drawableRes)
    }
}