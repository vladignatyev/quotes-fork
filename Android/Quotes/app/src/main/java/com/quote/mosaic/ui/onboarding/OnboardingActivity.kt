package com.quote.mosaic.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.common.utils.TimedActionConfirmHelper
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.data.manager.UserManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.onboarding_activity.*
import javax.inject.Inject

class OnboardingActivity : AppActivity(), HasAndroidInjector {

    @Inject
    lateinit var vmFactory: OnboardingViewModel.Factory

    @Inject
    lateinit var appExitTimer: TimedActionConfirmHelper

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var vm: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_activity)
        window.statusBarColor = ContextCompat.getColor(this, R.color.darkBlue)
        userPreferences.setBackgroundColor(R.color.bar_background_blue)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(OnboardingViewModel::class.java)
        vm.init()

        appExitTimer.setListener { finishAffinity() }
    }

    override fun onStart() {
        super.onStart()
        vm.state.loginSuccess.subscribe {
            val hostFragment = onboardingContainer as NavHostFragment
            hostFragment.navController.navigate(R.id.action_loginFragment_to_onboardingCategoryFragment)
        }.untilStopped()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        if (fragment != null && fragment.childFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else {
            promptToCloseApp()
        }
    }

    private fun promptToCloseApp() {
        val notify = appExitTimer.onAction()
        if (notify) {
            Toast.makeText(this, R.string.shared_button_press_again_to_exit, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {
        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }
}