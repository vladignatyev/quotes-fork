package com.quote.mosaic.core.di.module

import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.BillingManager
import com.quote.mosaic.ui.game.GameViewModel
import com.quote.mosaic.ui.game.hint.HintViewModel
import com.quote.mosaic.ui.game.success.GameSuccessViewModel
import com.quote.mosaic.ui.main.MainViewModel
import com.quote.mosaic.ui.main.play.OverviewViewModel
import com.quote.mosaic.ui.main.play.topic.TopicMapper
import com.quote.mosaic.ui.main.play.topic.TopicViewModel
import com.quote.mosaic.ui.main.play.topup.TopupViewModel
import com.quote.mosaic.ui.main.profile.ProfileViewModel
import com.quote.mosaic.ui.main.top.TopViewModel
import com.quote.mosaic.ui.onboarding.login.LoginViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

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
        apiClient: ApiClient
    ) = TopupViewModel.Factory(
        schedulers,
        apiClient
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

    @Provides
    @Singleton
    fun hintViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = HintViewModel.Factory(
        schedulers,
        apiClient
    )

    @Provides
    @Singleton
    fun successViewModelFactory(
        schedulers: Schedulers,
        apiClient: ApiClient
    ) = GameSuccessViewModel.Factory(
        schedulers,
        apiClient
    )

}