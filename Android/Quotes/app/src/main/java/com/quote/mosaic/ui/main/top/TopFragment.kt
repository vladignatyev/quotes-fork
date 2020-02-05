package com.quote.mosaic.ui.main.top

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.databinding.TopFragmentBinding
import javax.inject.Inject

class TopFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: TopViewModel.Factory

    private val vm: TopViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        updateBackgroundColor(this.container)
    }.root

    override fun onStart() {
        super.onStart()
        vm.load()
    }

}