package com.brain.words.puzzle.quotes.ui.main.top

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppFragment
import com.brain.words.puzzle.quotes.databinding.TopFragmentBinding
import javax.inject.Inject

class TopFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: TopViewModel.Factory

    private lateinit var vm: TopViewModel

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
                .of(this, vmFactory)
                .get(TopViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<TopFragmentBinding>(
        inflater, R.layout.top_fragment, container, false
    ).apply {
        fragment = this@TopFragment
        viewModel = vm
    }.root

}