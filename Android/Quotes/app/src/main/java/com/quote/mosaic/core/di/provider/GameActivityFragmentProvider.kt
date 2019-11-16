package com.quote.mosaic.core.di.provider

import com.quote.mosaic.ui.game.lamp.GameLampFragment
import com.quote.mosaic.ui.game.lamp.buy.GameBuyFragment
import com.quote.mosaic.ui.game.lamp.hints.GameHintsFragment
import com.quote.mosaic.ui.game.success.GameSuccessFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GameActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsGameBuyFragment(): GameBuyFragment

    @ContributesAndroidInjector
    abstract fun bindsGameHintsFragment(): GameHintsFragment

    @ContributesAndroidInjector
    abstract fun bindsGameLampFragment(): GameLampFragment

    @ContributesAndroidInjector
    abstract fun bindsGameSuccessFragment(): GameSuccessFragment
}