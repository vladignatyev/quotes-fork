package com.quote.mosaic.ui

import android.os.Bundle
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.onboarding.OnboardingActivity
import javax.inject.Inject

class SplashActivity : AppActivity() {

    @Inject
    lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NoActionBar)
        super.onCreate(savedInstanceState)

        if (userManager.getSession().isEmpty()) {
            startActivity(OnboardingActivity.newIntent(this))
        } else {
            startActivity(MainActivity.newIntent(this))
        }
    }

}