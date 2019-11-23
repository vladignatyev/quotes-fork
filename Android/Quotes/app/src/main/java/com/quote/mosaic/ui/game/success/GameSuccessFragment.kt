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
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppDialogFragment
import com.quote.mosaic.core.common.args
import com.quote.mosaic.databinding.GameSuccessFragmentBinding
import javax.inject.Inject

class GameSuccessFragment : AppDialogFragment() {

    @Inject
    lateinit var vmFactory: GameSuccessViewModel.Factory

    private lateinit var vm: GameSuccessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(GameSuccessViewModel::class.java)
        vm.setUp(args().getParcelable(KEY_QUOTE_SUCCESS)!!)
        vm.init()
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
        fragment = this@GameSuccessFragment
        viewModel = vm
        isCancelable = false
    }.root

    companion object {
        const val KEY_QUOTE_SUCCESS = "KEY_QUOTE_SUCCESS"
    }
}