package com.quote.mosaic.core.binding

import androidx.databinding.BindingAdapter
import com.quote.mosaic.core.ui.button.GoldPreloaderButton
import com.quote.mosaic.core.ui.button.GreenPreloaderButton
import com.quote.mosaic.core.ui.button.PreloaderButton

class PreloaderButtonBindingAdapters {

    @BindingAdapter("buttonEnabled", "buttonLoading", "buttonText", requireAll = true)
    fun configurePreloaderButton(button: PreloaderButton, enabled: Boolean, loading: Boolean, text: String) {
        button.configure(enabled, loading, text)
    }

    @BindingAdapter("buttonEnabled", "buttonLoading", "buttonText", requireAll = true)
    fun configureGoldPreloaderButton(button: GoldPreloaderButton, enabled: Boolean, loading: Boolean, text: String) {
        button.configure(enabled, loading, text)
    }

    @BindingAdapter("buttonEnabled", "buttonLoading", "buttonText", requireAll = true)
    fun configureGreenPreloaderButton(button: GreenPreloaderButton, enabled: Boolean, loading: Boolean, text: String) {
        button.configure(enabled, loading, text)
    }
}