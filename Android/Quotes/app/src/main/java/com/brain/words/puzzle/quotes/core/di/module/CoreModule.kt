package com.brain.words.puzzle.quotes.core.di.module

import android.content.Context
import android.content.SharedPreferences
import com.brain.words.puzzle.crypto.fs.sp.PasswordProvider
import com.brain.words.puzzle.crypto.fs.sp.SecurePreferences
import com.brain.words.puzzle.data.storage.KeyValueStorage
import com.brain.words.puzzle.data.storage.PreferencesStorage
import com.brain.words.puzzle.quotes.core.App
import com.brain.words.puzzle.quotes.core.common.utils.TimedActionConfirmHelper
import com.brain.words.puzzle.quotes.core.crypto.InstanceIdBackedPasswordProvider
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