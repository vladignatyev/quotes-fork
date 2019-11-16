package com.quote.mosaic.core.di.module

import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.ui.main.play.topic.TopicMapper
import com.quote.mosaic.ui.main.play.topic.TopicMapperImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MapperModule {

    @Provides
    @Singleton
    fun providesTopicMapper(
        userPreferences: UserPreferences
    ): TopicMapper = TopicMapperImpl(userPreferences)

}