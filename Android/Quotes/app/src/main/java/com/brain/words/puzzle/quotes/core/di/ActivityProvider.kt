package com.brain.words.puzzle.quotes.core.di

import com.brain.words.puzzle.quotes.ui.main.MainActivity
import com.brain.words.puzzle.quotes.ui.onboarding.OnboardingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityProvider {

    @ContributesAndroidInjector(modules = [OnboardingActivityFragmentProvider::class])
    abstract fun bindOnboardingActivity(): OnboardingActivity

    @ContributesAndroidInjector(modules = [MainActivityFragmentProvider::class])
    abstract fun bindMainActivity(): MainActivity

}