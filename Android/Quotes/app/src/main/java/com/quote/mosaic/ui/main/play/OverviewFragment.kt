package com.quote.mosaic.ui.main.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.manager.AdsManager
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.databinding.OverviewFragmentBinding
import javax.inject.Inject

class OverviewFragment : AppFragment() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var vmFactory: OverviewViewModel.Factory

    @Inject
    lateinit var adsManager: AdsManager

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding().topics.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                adsManager.showMainChangeTopicInter()
                analyticsManager.logTopicChanged(adapter.getPageTitle(position))
            }
        })
    }

    override fun onStart() {
        super.onStart()
        vm.state.categories.subscribe {
            analyticsManager.logTopicChanged(it.first().title)
            adapter.submitList(it)
        }.untilStopped()

        userManager.lowBalanceTrigger().subscribe {
            topupClicked("Main Screen with Low balance")
        }.untilStopped()
    }

    override fun onResume() {
        super.onResume()
        analyticsManager.logCurrentScreen(requireActivity(), "Overview Screen")
        billingManager.warmUp()
    }

    fun topupClicked() {
        topupClicked("Main Screen")
    }

    fun retry() {
        vm.load()
        adsManager.showErrorRetryInter()
    }

    private fun topupClicked(source: String) {
        analyticsManager.logTopupScreenOpened(source)
        val extras = FragmentNavigatorExtras(
            binding().title to "title",
            binding().balance to "balance",
            binding().topup to "topup"
        )

        if (findNavController().currentDestination?.id == R.id.overviewFragment) {
            findNavController().navigate(
                R.id.action_overviewFragment_to_topUpFragment, null, null, extras
            )
        }
    }

    private fun binding() = viewBinding<OverviewFragmentBinding>()
}