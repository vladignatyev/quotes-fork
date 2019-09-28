package com.brain.words.puzzle.quotes.core.di.module

import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.ui.onboarding.WelcomeViewModel
import com.brain.words.puzzle.quotes.ui.onboarding.login.LoginViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    fun welcomeViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = WelcomeViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun loginViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = LoginViewModel.Factory(
        schedulers,
        apiClient
    )
}