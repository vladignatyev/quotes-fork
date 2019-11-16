package com.quote.mosaic.core.ui

import android.graphics.drawable.Drawable

object DrawableUtils {

    fun clearState(drawable: Drawable?) {
        if (drawable != null) {
            drawable.state = intArrayOf()
        }
    }
}