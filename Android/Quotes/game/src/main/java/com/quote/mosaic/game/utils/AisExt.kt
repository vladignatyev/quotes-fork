package com.quote.mosaic.game.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes


fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

inline fun <reified T> emptyArrayList(): ArrayList<T> = ArrayList()

typealias AisClickListener = (String) -> Unit