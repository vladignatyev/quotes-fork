package com.quote.mosaic.ui.onboarding

import android.app.AlertDialog
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.common.utils.Ime
import com.quote.mosaic.databinding.OnboardingActivityBinding
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.onboarding.game.OnboardingGameFragment
import com.quote.mosaic.ui.onboarding.login.LoginFragment
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

    override fun onStart() {
        super.onStart()
        vm.state.successTrigger.subscribe {
            startActivity(MainActivity.newIntent(this))
        }.untilStopped()
    }

    override fun firstStepCompleted() {
        setStatusBarColor(R.color.darkPurple)
        Ime.hide(binding.root)
        binding.pager.currentItem = 1
    }

    override fun onGameCompleted() {
        AlertDialog
            .Builder(this)
            .setTitle("Ура!")
            .setMessage("Молодец, ты прошел обучение. Забери свои 100 монет")
            .setPositiveButton("Беру!") { _, _ ->
                startActivity(MainActivity.newIntent(this))
            }
            .show()
        setStatusBarColor(R.color.darkIndigo)
    }

    private fun setStatusBarColor(colorRes: Int) {
        window.statusBarColor = ContextCompat.getColor(this, colorRes)
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector
}