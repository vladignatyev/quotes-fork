package com.quote.mosaic.ui.game.lamp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppDialogFragment
import com.quote.mosaic.databinding.GameLampFragmentBinding
import javax.inject.Inject

class GameLampFragment : AppDialogFragment() {

    @Inject
    lateinit var vmFactory: GameLampViewModel.Factory

    private lateinit var vm: GameLampViewModel

    private lateinit var adapter: GameLampPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(GameLampViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<GameLampFragmentBinding>(
        inflater, R.layout.game_lamp_fragment, container, false
    ).apply {
        fragment = this@GameLampFragment
        viewModel = vm
        adapter = GameLampPagerAdapter(childFragmentManager, requireContext())
        pager.adapter = adapter
    }.root

    companion object {
        fun newInstance() = GameLampFragment()
    }

}