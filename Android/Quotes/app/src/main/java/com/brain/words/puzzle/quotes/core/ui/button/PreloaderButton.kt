package com.brain.words.puzzle.quotes.core.ui.button

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.binding.setVisible
import com.brain.words.puzzle.quotes.databinding.ButtonPreloaderBinding

class PreloaderButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ButtonPreloaderBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.button_preloader,
        this@PreloaderButton,
        true
    )

    fun configure(enabled: Boolean, inProgress: Boolean, text: String) {
        binding.container.isEnabled = enabled
        binding.progressbar.setVisible(inProgress)
        binding.text.setVisible(!inProgress)
        binding.text.text = text
    }
}