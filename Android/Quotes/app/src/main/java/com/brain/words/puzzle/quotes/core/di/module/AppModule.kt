package com.brain.words.puzzle.quotes.core.di.module

import com.brain.words.puzzle.quotes.core.PlatformSchedulers
import com.brain.words.puzzle.quotes.core.Schedulers
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        CoreModule::class,
        NetworkModule::class,
        ViewModelModule::class,
        ManagerModule::class,
        AppBindingModule::class
    ]
)
class AppModule {

    @Provides
    @Singleton
    fun rxSchedulers(): Schedulers = PlatformSchedulers
}