package com.quote.mosaic.core.di.module

import com.quote.mosaic.data.UserManager
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.data.manager.BillingManager
import com.quote.mosaic.ui.main.MainViewModel
import com.quote.mosaic.ui.main.play.OverviewViewModel
import com.quote.mosaic.ui.game.GameViewModel
import com.quote.mosaic.ui.game.lamp.GameLampViewModel
import com.quote.mosaic.ui.game.lamp.buy.GameBuyViewModel
import com.quote.mosaic.ui.game.lamp.hints.GameHintsViewModel
import com.quote.mosaic.ui.main.play.topic.TopicViewModel
import com.quote.mosaic.ui.main.play.topup.TopupViewModel
import com.quote.mosaic.ui.main.profile.ProfileViewModel
import com.quote.mosaic.ui.main.top.TopViewModel
import com.quote.mosaic.ui.onboarding.OnboardingViewModel
import com.quote.mosaic.ui.onboarding.login.LoginViewModel
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
    fun gameViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = GameViewModel.Factory(
        schedulers,
        apiClient
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
        userManager: UserManager
    ) = TopicViewModel.Factory(
        schedulers,
        apiClient,
        userManager
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
    fun gameHintModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = GameLampViewModel.Factory(
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

    @Provides
    @Singleton
    fun gameHintsViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = GameHintsViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun gameButViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient,
        billingManager: BillingManager
    ) = GameBuyViewModel.Factory(
        schedulers,
        apiClient,
        billingManager
    )

}