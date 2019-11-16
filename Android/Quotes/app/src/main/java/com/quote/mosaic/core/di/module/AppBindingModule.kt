package com.quote.mosaic.core.di.module

import com.quote.mosaic.core.binding.*
import com.quote.mosaic.core.di.AppDataBindingComponent
import dagger.Module
import dagger.Provides

@Module
class AppBindingModule {

    @Provides
    fun provideDataBindingComponent(): AppDataBindingComponent = DefaultAppDataBindingComponent()

    class DefaultAppDataBindingComponent : AppDataBindingComponent {

        override fun getViewBindingAdapters() = ViewBindingAdapters()

        override fun getImageViewBindingAdapters() = ImageViewBindingAdapters()

        override fun getConstraintLayoutBindingAdapters() = ConstraintLayoutBindingAdapters()

        override fun getViewPagerBindingAdapters() = ViewPagerBindingAdapters()

        override fun getPreloaderButtonBindingAdapters() = PreloaderButtonBindingAdapters()

        override fun getProgressBarBindingAdapters() = ProgressBarBindingAdapters()

        override fun getTextViewBindingAdapters() = TextViewBindingAdapters()

        override fun getRecyclerViewBindingAdapters() = RecyclerViewBindingAdapters()
    }

}