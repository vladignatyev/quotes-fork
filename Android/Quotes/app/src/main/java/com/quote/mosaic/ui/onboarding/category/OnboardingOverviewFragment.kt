package com.quote.mosaic.ui.onboarding.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.manager.AdsManager
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.databinding.OnboardingOverviewFragmentBinding
import com.quote.mosaic.ui.main.play.topic.TopicModel
import com.quote.mosaic.ui.onboarding.OnboardingViewModel
import javax.inject.Inject

class OnboardingOverviewFragment : AppFragment() {

    @Inject lateinit var adsManager: AdsManager

    @Inject lateinit var analyticsManager: AnalyticsManager

    private val vm: OnboardingViewModel by activityViewModels()

    private lateinit var adapter: OOPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adsManager.showOnboardingNameInter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<OnboardingOverviewFragmentBinding>(
        inflater, R.layout.onboarding_overview_fragment, container, false
    ).apply {
        fragment = this@OnboardingOverviewFragment
        viewModel = vm
        adapter = OOPagerAdapter(childFragmentManager)
        topics.adapter = adapter
        adapter.submitList(listOf(TopicModel(0, "")))
    }.root

    override fun onResume() {
        super.onResume()
        analyticsManager.logCurrentScreen(requireActivity(), "Onboarding Overview Screen")
    }
}