package com.quote.mosaic.core.di.provider

import com.quote.mosaic.ui.main.play.OverviewFragment
import com.quote.mosaic.ui.main.play.topic.TopicFragment
import com.quote.mosaic.ui.main.play.topup.TopUpFragment
import com.quote.mosaic.ui.main.profile.ProfileFragment
import com.quote.mosaic.ui.main.top.TopFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsTopicFragment(): TopicFragment

    @ContributesAndroidInjector
    abstract fun bindsTopupFragment(): TopUpFragment

    @ContributesAndroidInjector
    abstract fun bindsOverviewFragment(): OverviewFragment

    @ContributesAndroidInjector
    abstract fun bindsTopFragment(): TopFragment

    @ContributesAndroidInjector
    abstract fun bindsProfileFragment(): ProfileFragment
}