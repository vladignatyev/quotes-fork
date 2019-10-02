package com.brain.words.puzzle.quotes.core.di.module

import com.brain.words.puzzle.quotes.core.manager.FirebaseManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ManagerModule {

    @Provides
    @Singleton
    fun providesFirebaseManager(): FirebaseManager = FirebaseManager()
}