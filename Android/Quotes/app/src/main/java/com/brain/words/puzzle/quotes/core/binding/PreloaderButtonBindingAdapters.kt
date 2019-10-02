package com.brain.words.puzzle.quotes.core.binding

import androidx.databinding.BindingAdapter
import com.brain.words.puzzle.quotes.core.ui.button.PreloaderButton

class PreloaderButtonBindingAdapters {

    @BindingAdapter("buttonEnabled", "buttonLoading", "buttonText", requireAll = true)
    fun configurePreloaderButton(button: PreloaderButton, enabled: Boolean, loading: Boolean, text: String) {
        button.configure(enabled, loading, text)
    }
}