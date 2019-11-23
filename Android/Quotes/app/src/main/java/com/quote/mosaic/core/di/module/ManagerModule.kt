package com.quote.mosaic.core.di.module

import android.content.Context
import android.content.SharedPreferences
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.crypto.fs.sp.SecurePreferences
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.core.manager.FirebaseManager
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.core.manager.billing.BillingManager
import com.quote.mosaic.core.manager.billing.BillingManagerImpl
import com.quote.mosaic.data.api.ApiClient
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
    ): BillingManager = BillingManagerImpl(context, schedulers, apiClient)
}