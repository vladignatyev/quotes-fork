package com.quote.mosaic.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.tabs.TabLayout

class NonTouchableTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TabLayout(context, attrs) {

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return true
    }
}