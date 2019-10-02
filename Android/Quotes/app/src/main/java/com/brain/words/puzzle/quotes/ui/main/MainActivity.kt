package com.brain.words.puzzle.quotes.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppActivity
import com.brain.words.puzzle.quotes.core.common.utils.TimedActionConfirmHelper
import com.brain.words.puzzle.quotes.core.ext.setupWithNavController
import com.brain.words.puzzle.quotes.databinding.MainActivityBinding
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MainActivity : AppActivity(), HasAndroidInjector {

    @Inject
    lateinit var appExitTimer: TimedActionConfirmHelper

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

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

    fun promptToCloseApp() {
        val notify = appExitTimer.onAction()
        if (notify) {
            Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigationBar() {
        binding.bottomBar.itemIconTintList = null
        binding.bottomBar.setupWithNavController(
            navGraphIds = listOf(R.navigation.game, R.navigation.top, R.navigation.profile),
            fragmentManager = supportFragmentManager,
            containerId = R.id.fragmentContainer,
            intent = intent
        )
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
