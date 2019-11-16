package com.quote.mosaic.core.binding

import android.animation.ValueAnimator
import android.widget.TextView
import androidx.databinding.BindingAdapter

class TextViewBindingAdapters {

    @BindingAdapter("animatePercent")
    fun setAnimatePercents(view: TextView, finalValue: Int) {
        ValueAnimator.ofInt(0, 0).apply {
            duration = 2000
            setIntValues(0, finalValue)
            addUpdateListener {
                view.text = "${it.animatedValue}%"
            }
        }.start()
    }
}