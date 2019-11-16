package com.quote.mosaic.core.di.provider

import com.quote.mosaic.ui.SplashActivity
import com.quote.mosaic.ui.game.GameActivity
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.onboarding.OnboardingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityProvider {

    @ContributesAndroidInjector(modules = [OnboardingActivityFragmentProvider::class])
    abstract fun bindOnboardingActivity(): OnboardingActivity

    @ContributesAndroidInjector(modules = [MainActivityFragmentProvider::class])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [GameActivityFragmentProvider::class])
    abstract fun bindGameActivity(): GameActivity

    @ContributesAndroidInjector
    abstract fun bindSplashActivity(): SplashActivity

}