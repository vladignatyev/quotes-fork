package com.quote.mosaic.ui.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.databinding.OnboardingLoginFragmentBinding
import com.quote.mosaic.ui.onboarding.OnboardingViewModel
import javax.inject.Inject

class LoginFragment : AppFragment() {

    @Inject lateinit var analyticsManager: AnalyticsManager

    private val vm: OnboardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsManager.logOnboardingNameStarted()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<OnboardingLoginFragmentBinding>(
        inflater, R.layout.onboarding_login_fragment, container, false
    ).apply {
        fragment = this@LoginFragment
        viewModel = vm
    }.root

    override fun onResume() {
        super.onResume()
        analyticsManager.logCurrentScreen(requireActivity(), "Onboarding Enter Name Screen")
    }
}