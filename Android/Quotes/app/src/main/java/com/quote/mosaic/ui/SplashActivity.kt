package com.quote.mosaic.ui

import android.os.Bundle
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.manager.AdsManager
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.onboarding.OnboardingActivity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashActivity : AppActivity() {

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        if (userManager.getSession().isEmpty()) {
            startActivity(OnboardingActivity.newIntent(this))
        } else {
            val start = System.nanoTime()
            adsManager.loadInterSplash(
                onWatched = {
                    startActivity(MainActivity.newIntent(this))
                },
                onLoaded = {
                    val diffSeconds =
                        TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS)
                    analyticsManager.logSplashLoadingTime(diffSeconds.toString())
                }
            )
        }
    }

}