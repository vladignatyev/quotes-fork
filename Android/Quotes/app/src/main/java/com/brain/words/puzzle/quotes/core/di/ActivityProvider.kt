package com.brain.words.puzzle.quotes.core.di

import com.brain.words.puzzle.quotes.ui.onboarding.WelcomeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityProvider {

    @ContributesAndroidInjector(modules = [WelcomeActivityFragmentProvider::class])
    abstract fun bindWelcomeActivity(): WelcomeActivity

}