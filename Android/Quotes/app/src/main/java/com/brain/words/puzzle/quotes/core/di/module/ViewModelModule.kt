package com.brain.words.puzzle.quotes.core.di.module

import com.brain.words.puzzle.data.UserManager
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.ui.main.MainViewModel
import com.brain.words.puzzle.quotes.ui.main.play.OverviewViewModel
import com.brain.words.puzzle.quotes.ui.main.play.topic.TopicViewModel
import com.brain.words.puzzle.quotes.ui.main.play.topup.TopupViewModel
import com.brain.words.puzzle.quotes.ui.main.profile.ProfileViewModel
import com.brain.words.puzzle.quotes.ui.main.top.TopViewModel
import com.brain.words.puzzle.quotes.ui.onboarding.OnboardingViewModel
import com.brain.words.puzzle.quotes.ui.onboarding.login.LoginViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    fun onboardingViewModelFactory(
        userManager: UserManager
    ) = OnboardingViewModel.Factory(
        userManager
    )

    @Provides
    @Singleton
    fun loginViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        userManager: UserManager
    ) = LoginViewModel.Factory(
        schedulers,
        apiClient,
        userManager
    )

    @Provides
    @Singleton
    fun mainViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = MainViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun overviewViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = OverviewViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun topicViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = TopicViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun topViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = TopViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun topupViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = TopupViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun profileViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = ProfileViewModel.Factory(
        schedulers,
        apiClient
    )

}