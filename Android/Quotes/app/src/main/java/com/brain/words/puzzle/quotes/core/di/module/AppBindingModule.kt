package com.brain.words.puzzle.quotes.core.di.module

import com.brain.words.puzzle.quotes.core.binding.*
import com.brain.words.puzzle.quotes.core.di.AppDataBindingComponent
import dagger.Module
import dagger.Provides

@Module
class AppBindingModule {

    @Provides
    fun provideDataBindingComponent(): AppDataBindingComponent = DefaultAppDataBindingComponent()

    class DefaultAppDataBindingComponent : AppDataBindingComponent {

        override fun getViewBindingAdapters() = ViewBindingAdapters()

        override fun getImageViewBindingAdapters() = ImageViewBindingAdapters()

        override fun getViewPagerBindingAdapters() = ViewPagerBindingAdapters()

        override fun getPreloaderButtonBindingAdapters() = PreloaderButtonBindingAdapters()

        override fun getRecyclerViewBindingAdapters() = RecyclerViewBindingAdapters()
    }

}