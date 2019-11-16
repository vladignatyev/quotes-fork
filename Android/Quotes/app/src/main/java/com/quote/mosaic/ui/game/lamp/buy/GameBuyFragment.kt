package com.quote.mosaic.ui.game.lamp.buy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.databinding.GameBuyFragmentBinding
import javax.inject.Inject

class GameBuyFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: GameBuyViewModel.Factory

    private lateinit var vm: GameBuyViewModel

    private lateinit var adapter: GameBuyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(GameBuyViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<GameBuyFragmentBinding>(
        inflater, R.layout.game_buy_fragment, container, false
    ).apply {
        viewModel = vm
        adapter = GameBuyAdapter({}, {})
        items.adapter = adapter
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.items.subscribe {
            adapter.submitList(it)
        }.untilStopped()
    }

    companion object {
        fun newInstance() = GameBuyFragment()
    }
}