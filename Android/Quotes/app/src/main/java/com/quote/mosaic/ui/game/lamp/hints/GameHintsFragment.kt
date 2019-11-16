package com.quote.mosaic.ui.game.lamp.hints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.databinding.GameHintsFragmentBinding
import com.quote.mosaic.ui.game.lamp.buy.GameBuyAdapter
import javax.inject.Inject

class GameHintsFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: GameHintsViewModel.Factory

    private lateinit var vm: GameHintsViewModel

    private lateinit var adapter: GameHintsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(GameHintsViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<GameHintsFragmentBinding>(
        inflater, R.layout.game_hints_fragment, container, false
    ).apply {
        fragment = this@GameHintsFragment
        viewModel = vm
        adapter = GameHintsAdapter {}
        items.adapter = adapter
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.items.subscribe {
            adapter.submitList(it)
        }.untilStopped()
    }

    companion object {
        fun newInstance() = GameHintsFragment()
    }
}