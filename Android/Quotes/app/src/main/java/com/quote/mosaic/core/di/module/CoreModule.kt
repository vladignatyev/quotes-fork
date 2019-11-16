package com.quote.mosaic.core.di.module

import android.content.Context
import android.content.SharedPreferences
import com.quote.mosaic.crypto.fs.sp.PasswordProvider
import com.quote.mosaic.crypto.fs.sp.SecurePreferences
import com.quote.mosaic.data.storage.KeyValueStorage
import com.quote.mosaic.data.storage.PreferencesStorage
import com.quote.mosaic.core.App
import com.quote.mosaic.core.common.utils.TimedActionConfirmHelper
import com.quote.mosaic.core.crypto.InstanceIdBackedPasswordProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CoreModule {

    @Provides
    @Singleton
    fun sharedPreferences(app: App): SharedPreferences = app.getSharedPreferences(
        "shared_prefs", Context.MODE_PRIVATE
    )

    @Provides
    fun timedActionConfirmHelper() = TimedActionConfirmHelper(TimedActionConfirmHelper.TIMEOUT_DEFAULT)

    @Provides
    @Singleton
    fun securePreferences(
        context: Context, passwordProvider: PasswordProvider
    ): SecurePreferences = SecurePreferences.default(context, passwordProvider)

    @Provides
    @Singleton
    fun passwordProvider(): PasswordProvider = InstanceIdBackedPasswordProvider()

    @Provides
    @Singleton
    fun providePreferencesStorage(context: Context): KeyValueStorage = PreferencesStorage(context, PreferencesStorage.QUOTES_PREFERENCES_STORAGE)

}