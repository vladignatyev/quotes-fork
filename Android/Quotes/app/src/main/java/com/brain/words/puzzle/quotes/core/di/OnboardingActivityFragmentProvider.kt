package com.brain.words.puzzle.quotes.core.di

import com.brain.words.puzzle.quotes.ui.onboarding.game.OnboardingGameFragment
import com.brain.words.puzzle.quotes.ui.onboarding.login.LoginFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnboardingActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun bindsGameFragment(): OnboardingGameFragment
}