package com.quote.mosaic.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.common.utils.TimedActionConfirmHelper
import com.quote.mosaic.core.ext.setupWithNavController
import com.quote.mosaic.R
import com.quote.mosaic.core.manager.AdsManager
import com.quote.mosaic.databinding.MainActivityBinding
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MainActivity : AppActivity(), HasAndroidInjector {

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var appExitTimer: TimedActionConfirmHelper

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        appExitTimer.setListener {
            finishAffinity()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
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

    private fun setupBottomNavigationBar() {
        binding.bottomBar.itemIconTintList = null
        binding.bottomBar.setupWithNavController(
            navGraphIds = listOf(R.navigation.overview, R.navigation.top, R.navigation.profile),
            fragmentManager = supportFragmentManager,
            containerId = R.id.fragmentContainer,
            intent = intent,
            onChanged = { adsManager.showMainNavigationInter() }
        )
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
