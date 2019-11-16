package com.quote.mosaic.core.di.provider

import com.quote.mosaic.ui.main.play.OverviewFragment
import com.quote.mosaic.ui.game.lamp.GameLampFragment
import com.quote.mosaic.ui.game.lamp.buy.GameBuyFragment
import com.quote.mosaic.ui.game.lamp.hints.GameHintsFragment
import com.quote.mosaic.ui.game.success.GameSuccessFragment
import com.quote.mosaic.ui.main.play.topic.TopicFragment
import com.quote.mosaic.ui.main.play.topup.TopupFragment
import com.quote.mosaic.ui.main.profile.ProfileFragment
import com.quote.mosaic.ui.main.top.TopFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsTopicFragment(): TopicFragment

    @ContributesAndroidInjector
    abstract fun bindsTopupFragment(): TopupFragment

    @ContributesAndroidInjector
    abstract fun bindsOverviewFragment(): OverviewFragment

    @ContributesAndroidInjector
    abstract fun bindsGameHintFragment(): GameLampFragment

    @ContributesAndroidInjector
    abstract fun bindsGameSuccessFragment(): GameSuccessFragment

    @ContributesAndroidInjector
    abstract fun bindsTopFragment(): TopFragment

    @ContributesAndroidInjector
    abstract fun bindsProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun bindsGameBuyFragment(): GameBuyFragment

    @ContributesAndroidInjector
    abstract fun bindsGameHintsFragment(): GameHintsFragment
}