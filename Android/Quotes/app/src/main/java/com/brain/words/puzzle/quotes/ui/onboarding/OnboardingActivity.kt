package com.brain.words.puzzle.quotes.ui.onboarding

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppActivity
import com.brain.words.puzzle.quotes.core.common.utils.Ime
import com.brain.words.puzzle.quotes.databinding.OnboardingActivityBinding
import com.brain.words.puzzle.quotes.ui.onboarding.game.OnboardingGameFragment
import com.brain.words.puzzle.quotes.ui.onboarding.login.LoginFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class OnboardingActivity : AppActivity(), HasAndroidInjector,
    LoginFragment.Listener,
    OnboardingGameFragment.Listener {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var vmFactory: OnboardingViewModel.Factory

    private lateinit var vm: OnboardingViewModel

    private lateinit var binding: OnboardingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(OnboardingViewModel::class.java)
        vm.init()

        setStatusBarColor(R.color.darkBlue)
        binding = DataBindingUtil.setContentView<OnboardingActivityBinding>(
            this, R.layout.onboarding_activity
        ).apply {
            activity = this@OnboardingActivity
            viewModel = vm
            pager.adapter = OnboardingPagerAdapter(supportFragmentManager)
        }
    }

    override fun onLoginCompleted() {
        Ime.hide(binding.root)
        binding.pager.currentItem = 1
        setStatusBarColor(R.color.darkPurple)
    }

    override fun onGameCompleted() {
        binding.pager.currentItem = 1
        setStatusBarColor(R.color.darkIndigo)
    }

    private fun setStatusBarColor(colorRes: Int) {
        window.statusBarColor = ContextCompat.getColor(this, colorRes)
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector
}