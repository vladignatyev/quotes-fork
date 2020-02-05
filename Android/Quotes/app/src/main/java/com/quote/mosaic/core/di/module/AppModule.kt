package com.quote.mosaic.core.di.module

import com.quote.mosaic.core.PlatformSchedulers
import com.quote.mosaic.core.Schedulers
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        CoreModule::class,
        NetworkModule::class,
        ViewModelModule::class,
        ManagerModule::class,
        MapperModule::class,
        AppBindingModule::class
    ]
)
class AppModule {

    @Provides
    @Singleton
    fun rxSchedulers(): Schedulers = PlatformSchedulers
}