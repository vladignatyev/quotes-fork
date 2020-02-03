package com.quote.mosaic.core.di.module

import android.content.Context
import android.content.SharedPreferences
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.billing.InAppBillingManager
import com.quote.mosaic.core.manager.*
import com.quote.mosaic.crypto.fs.sp.SecurePreferences
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.ui.onboarding.RandomNameGenerator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ManagerModule {

    @Provides
    @Singleton
    fun providesFirebaseManager(userManager: UserManager): FirebaseManager =
        FirebaseManager(userManager)

    @Provides
    @Singleton
    fun providesUserManager(
        securePreferences: SecurePreferences
    ): UserManager =
        UserManager(securePreferences)

    @Provides
    @Singleton
    fun provideUserPreferences(
        sharedPreferences: SharedPreferences
    ): UserPreferences = UserPreferences(sharedPreferences)

    @Provides
    @Singleton
    fun provideBillingManager(
        context: Context,
        schedulers: Schedulers,
        apiClient: ApiClient
    ): BillingManager = InAppBillingManager(context, schedulers, apiClient)

    @Provides
    @Singleton
    fun randomNameGenerator(
        context: Context
    ): RandomNameGenerator = RandomNameGenerator(context)

    @Provides
    @Singleton
    fun analyticsManager(
        context: Context
    ): AnalyticsManager = AnalyticsManagerImpl(context)

    @Provides
    @Singleton
    fun adsManager(
        context: Context
    ): AdsManager = AdsManagerImpl(context)
}