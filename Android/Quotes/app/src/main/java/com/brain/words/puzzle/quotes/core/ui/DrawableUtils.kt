package com.brain.words.puzzle.quotes.core.ui

import android.graphics.drawable.Drawable

object DrawableUtils {

    fun clearState(drawable: Drawable?) {
        if (drawable != null) {
            drawable.state = intArrayOf()
        }
    }
}