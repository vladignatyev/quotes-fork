package com.quote.mosaic.ui.game.success

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.quote.mosaic.R
import com.quote.mosaic.core.AppDialogFragment
import com.quote.mosaic.core.common.parentAs
import com.quote.mosaic.databinding.GameSuccessFragmentBinding

class GameSuccessFragment : AppDialogFragment() {

    private lateinit var listener: Listener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentAs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<GameSuccessFragmentBinding>(
        inflater, R.layout.game_success_fragment, container, false
    ).apply {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        listener = this@GameSuccessFragment.listener
        fragment = this@GameSuccessFragment
        isCancelable = false
    }.root

    fun onNextClicked() {
        dismiss()
        listener.onNextLevelClicked()
    }

    interface Listener {
        fun goMainClicked()
        fun onNextLevelClicked()
    }

    companion object {

        fun newInstance() = GameSuccessFragment()
    }
}