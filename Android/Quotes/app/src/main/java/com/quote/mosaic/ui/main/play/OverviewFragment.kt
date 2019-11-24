package com.quote.mosaic.ui.main.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.databinding.OverviewFragmentBinding
import javax.inject.Inject

class OverviewFragment : AppFragment() {

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var userManager: UserManager

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
        updateBackgroundColor(this.container, listOf(this.appBar))
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.categories.subscribe {
            adapter.submitList(it)
        }.untilStopped()

        userManager.lowBalanceTrigger().subscribe {
            topupClicked()
        }.untilStopped()
    }
    override fun onResume() {
        super.onResume()
        billingManager.warmUp()
    }

    fun topupClicked() {
        val extras = FragmentNavigatorExtras(
            binding().title to "title",
            binding().balance to "balance",
            binding().topup to "topup"
        )

        findNavController().navigate(
            R.id.action_overviewFragment_to_topUpFragment2, null, null, extras
        )
    }

    private fun binding() = viewBinding<OverviewFragmentBinding>()
}