package com.brain.words.puzzle.quotes.ui.main.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppFragment
import com.brain.words.puzzle.quotes.databinding.OverviewFragmentBinding
import javax.inject.Inject

class OverviewFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: OverviewViewModel.Factory

    private lateinit var vm: OverviewViewModel

    private lateinit var adapter: OverviewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(OverviewViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<OverviewFragmentBinding>(
        inflater, R.layout.overview_fragment, container, false
    ).apply {
        fragment = this@OverviewFragment
        viewModel = vm
        adapter = OverviewPagerAdapter(childFragmentManager)
        topics.adapter = adapter
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.categories.subscribe {
            adapter.submitList(it)
        }.untilStopped()
    }

    fun topupClicked() {
        findNavController().navigate(R.id.action_overviewFragment_to_topupFragment)
    }

    private fun binding() = viewBinding<OverviewFragmentBinding>()
}