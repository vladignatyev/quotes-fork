package com.brain.words.puzzle.quotes.core.di

import com.brain.words.puzzle.quotes.ui.onboarding.login.LoginFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class WelcomeActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsLoginFragment(): LoginFragment
}