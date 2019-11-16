package com.quote.mosaic.ui.onboarding

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.common.utils.Ime
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.databinding.OnboardingActivityBinding
import com.quote.mosaic.ui.SplashActivity
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
            .setPositiveButton("Беру!") { _, _ -> startActivity(MainActivity.newIntent(this)) }
            .show()
    }

    private fun setStatusBarColor(colorRes: Int) {
        window.statusBarColor = ContextCompat.getColor(this, colorRes)
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {

        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)

    }
}