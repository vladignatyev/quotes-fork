package com.quote.mosaic.core.di.provider

import com.quote.mosaic.ui.game.GameFragment
import com.quote.mosaic.ui.main.play.topup.TopUpFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GameActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsTopupFragment(): TopUpFragment

    @ContributesAndroidInjector
    abstract fun bindsGameFragment(): GameFragment
}