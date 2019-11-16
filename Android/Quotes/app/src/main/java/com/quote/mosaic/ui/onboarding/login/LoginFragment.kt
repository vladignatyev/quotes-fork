package com.quote.mosaic.ui.onboarding.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.common.parentAs
import com.quote.mosaic.databinding.OnboardingLoginFragmentBinding
import javax.inject.Inject

class LoginFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: LoginViewModel.Factory

    private lateinit var vm: LoginViewModel

    private lateinit var listener: Listener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentAs()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(LoginViewModel::class.java)
        vm.init()
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

    override fun onStart() {
        super.onStart()
        vm.state.loginSuccess.subscribe {
            listener.firstStepCompleted()
        }.untilStopped()
    }

    interface Listener {
        fun firstStepCompleted()
    }

    companion object {
        fun newInstance() = LoginFragment()
    }
}