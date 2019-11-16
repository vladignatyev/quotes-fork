package com.quote.mosaic.core.di.provider

import com.quote.mosaic.ui.onboarding.game.OnboardingGameFragment
import com.quote.mosaic.ui.onboarding.login.LoginFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnboardingActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun bindsGameFragment(): OnboardingGameFragment
}