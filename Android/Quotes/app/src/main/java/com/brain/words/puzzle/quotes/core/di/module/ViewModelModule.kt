package com.brain.words.puzzle.quotes.core.di.module

import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.core.manager.FirebaseManager
import com.brain.words.puzzle.quotes.ui.main.MainViewModel
import com.brain.words.puzzle.quotes.ui.main.game.GameViewModel
import com.brain.words.puzzle.quotes.ui.main.game.topic.TopicViewModel
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
    fun welcomeViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = OnboardingViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun loginViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        firebaseManager: FirebaseManager
    ) = LoginViewModel.Factory(
        schedulers,
        apiClient,
        firebaseManager
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
    fun gameViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = GameViewModel.Factory(
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
    fun profileViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = ProfileViewModel.Factory(
        schedulers,
        apiClient
    )

}