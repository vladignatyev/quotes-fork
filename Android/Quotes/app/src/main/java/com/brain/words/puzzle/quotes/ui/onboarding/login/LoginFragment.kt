package com.brain.words.puzzle.quotes.ui.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppFragment
import com.brain.words.puzzle.quotes.databinding.LoginFragmentBinding
import javax.inject.Inject

class LoginFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: LoginViewModel.Factory

    private lateinit var vm: LoginViewModel

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
    ): View = DataBindingUtil.inflate<LoginFragmentBinding>(
        inflater, R.layout.login_fragment, container, false
    ).apply {
        fragment = this@LoginFragment
        viewModel = vm
    }.root
}