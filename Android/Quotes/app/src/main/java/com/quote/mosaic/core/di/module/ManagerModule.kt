package com.quote.mosaic.core.di.module

import android.content.Context
import android.content.SharedPreferences
import com.quote.mosaic.crypto.fs.sp.SecurePreferences
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.core.manager.FirebaseManager
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.data.manager.BillingManager
import com.quote.mosaic.data.manager.BillingManagerImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ManagerModule {

    @Provides
    @Singleton
    fun providesFirebaseManager(): FirebaseManager = FirebaseManager()

    @Provides
    @Singleton
    fun providesUserManager(
        securePreferences: SecurePreferences
    ): UserManager = UserManager(securePreferences)

    @Provides
    @Singleton
    fun provideUserPreferences(
        sharedPreferences: SharedPreferences
    ): UserPreferences = UserPreferences(sharedPreferences)

    @Provides
    @Singleton
    fun provideBillingManager(context: Context): BillingManager = BillingManagerImpl(context)
}