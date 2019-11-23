package com.quote.mosaic.ui.onboarding

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.common.utils.Ime
import com.quote.mosaic.core.common.utils.TimedActionConfirmHelper
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.databinding.OnboardingActivityBinding
import com.quote.mosaic.databinding.OnboardingCompletePopupBinding
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.onboarding.game.OnboardingGameFragment
import com.quote.mosaic.ui.onboarding.login.LoginFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class OnboardingActivity : AppActivity(),
    HasAndroidInjector,
    LoginFragment.Listener,
    OnboardingGameFragment.Listener {

    @Inject
    lateinit var appExitTimer: TimedActionConfirmHelper

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var binding: OnboardingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarColor(R.color.darkBlue)

        binding = DataBindingUtil.setContentView<OnboardingActivityBinding>(
            this, R.layout.onboarding_activity
        ).apply {
            activity = this@OnboardingActivity
            pager.adapter = OnboardingPagerAdapter(supportFragmentManager)
        }

        appExitTimer.setListener {
            finishAffinity()
        }
    }

    override fun firstStepCompleted() {
        setStatusBarColor(R.color.darkPurple)
        Ime.hide(binding.root)
        binding.pager.currentItem = 1
    }

    override fun onGameCompleted() {
        val binding: OnboardingCompletePopupBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this), R.layout.onboarding_complete_popup, null, true
            )

        binding.grab.setOnClickListener {
            startActivity(MainActivity.newIntent(this))
        }

        AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(binding.root)
            .show()
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
            Toast.makeText(this, R.string.shared_button_press_again_to_exit, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setStatusBarColor(colorRes: Int) {
        window.statusBarColor = ContextCompat.getColor(this, colorRes)
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {

        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)

    }
}