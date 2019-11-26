package com.quote.mosaic.core.di.module

import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.ui.main.play.game.GameViewModel
import com.quote.mosaic.ui.main.MainViewModel
import com.quote.mosaic.ui.main.play.OverviewViewModel
import com.quote.mosaic.ui.main.play.topic.TopicMapper
import com.quote.mosaic.ui.main.play.topic.TopicViewModel
import com.quote.mosaic.ui.main.play.topup.TopUpProductMapper
import com.quote.mosaic.ui.main.play.topup.TopUpViewModel
import com.quote.mosaic.ui.main.profile.ProfileViewModel
import com.quote.mosaic.ui.main.top.TopViewModel
import com.quote.mosaic.ui.onboarding.OnboardingViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    fun onboardingViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        userManager: UserManager
    ) = OnboardingViewModel.Factory(
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
    fun gameViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        userManager: UserManager,
        billingManager: BillingManager
    ) = GameViewModel.Factory(
        schedulers,
        apiClient,
        userManager,
        billingManager
    )

    @Provides
    @Singleton
    fun overviewViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        userManager: UserManager
    ) = OverviewViewModel.Factory(
        schedulers,
        apiClient,
        userManager
    )

    @Provides
    @Singleton
    fun topicViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        userManager: UserManager,
        mapper: TopicMapper,
        userPreferences: UserPreferences
    ) = TopicViewModel.Factory(
        schedulers,
        apiClient,
        userManager,
        mapper,
        userPreferences
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
        apiClient: ApiClient,
        billingManager: BillingManager,
        userManager: UserManager,
        productsMapper: TopUpProductMapper
    ) = TopUpViewModel.Factory(
        schedulers,
        apiClient,
        billingManager,
        userManager,
        productsMapper
    )

    @Provides
    @Singleton
    fun profileViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        userManager: UserManager,
        userPreferences: UserPreferences
    ) = ProfileViewModel.Factory(
        schedulers,
        apiClient,
        userManager,
        userPreferences
    )

}