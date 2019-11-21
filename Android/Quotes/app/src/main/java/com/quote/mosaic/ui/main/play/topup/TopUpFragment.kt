package com.quote.mosaic.ui.main.play.topup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionInflater
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.common.args
import com.quote.mosaic.databinding.TopupFragmentBinding
import javax.inject.Inject

class TopUpFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: TopUpViewModel.Factory

    private lateinit var vm: TopUpViewModel

    private lateinit var adapter: TopUpProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(TopUpViewModel::class.java)
        vm.setUp(args().getString(KEY_USER_NAME), args().getString(KEY_USER_BALANCE))
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<TopupFragmentBinding>(
        inflater, R.layout.topup_fragment, container, false
    ).apply {
        updateBackgroundColor(this.container)
        fragment = this@TopUpFragment
        viewModel = vm

        adapter = TopUpProductsAdapter { vm.buyProduct(requireActivity(), it) }
        items.adapter = adapter
        items.addItemDecoration(TopUpProductsAdapter.decoration(10))
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.products.subscribe {
            adapter.submitList(it)
            binding().items.scheduleLayoutAnimation()
        }.untilStopped()
    }

    private fun binding() = viewBinding<TopupFragmentBinding>()

    companion object {
        const val KEY_USER_NAME = "KEY_USER_NAME"
        const val KEY_USER_BALANCE = "KEY_USER_BALANCE"
    }

}