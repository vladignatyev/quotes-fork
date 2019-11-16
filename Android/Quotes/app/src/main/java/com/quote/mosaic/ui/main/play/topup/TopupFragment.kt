package com.quote.mosaic.ui.main.play.topup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppDialogFragment
import com.quote.mosaic.databinding.TopupFragmentBinding
import javax.inject.Inject

class TopupFragment : AppDialogFragment() {

    @Inject
    lateinit var vmFactory: TopupViewModel.Factory

    private lateinit var vm: TopupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(TopupViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<TopupFragmentBinding>(
        inflater, R.layout.topup_fragment, container, false
    ).apply {
        fragment = this@TopupFragment
        viewModel = vm
    }.root

}