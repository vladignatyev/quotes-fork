package com.brain.words.puzzle.quotes.core.binding

import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.brain.words.puzzle.quotes.core.ui.anim.ProgressBarAnimation


class ProgressBarBindingAdapters {

    @BindingAdapter("setProgress")
    fun setProgress(progressBar: ProgressBar, current: Int) {
        val animation = ProgressBarAnimation(progressBar, 0f, current.toFloat()).apply {
            duration = 2000
            interpolator = LinearInterpolator()
        }
        progressBar.startAnimation(animation)
    }
}