package com.quote.mosaic.core.ui.button

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.quote.mosaic.R
import com.quote.mosaic.core.binding.setVisible
import com.quote.mosaic.databinding.ButtonPreloaderGreenBinding

class GreenPreloaderButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ButtonPreloaderGreenBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.button_preloader_green,
        this@GreenPreloaderButton,
        true
    )

    fun configure(enabled: Boolean, inProgress: Boolean, text: String) {
        binding.container.isEnabled = enabled
        binding.progressbar.setVisible(inProgress)
        binding.text.setVisible(!inProgress)
        binding.text.text = text
    }
}