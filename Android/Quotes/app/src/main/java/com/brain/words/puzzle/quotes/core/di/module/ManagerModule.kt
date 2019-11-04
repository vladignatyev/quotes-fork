package com.brain.words.puzzle.quotes.core.di.module

import com.brain.words.puzzle.crypto.fs.sp.SecurePreferences
import com.brain.words.puzzle.data.UserManager
import com.brain.words.puzzle.quotes.core.manager.FirebaseManager
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
}