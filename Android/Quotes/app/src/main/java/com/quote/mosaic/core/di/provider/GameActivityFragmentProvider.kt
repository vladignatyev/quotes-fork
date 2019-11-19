package com.quote.mosaic.core.di.provider

import com.quote.mosaic.ui.game.hint.HintFragment
import com.quote.mosaic.ui.game.success.GameSuccessFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GameActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsGameSuccessFragment(): GameSuccessFragment

    @ContributesAndroidInjector
    abstract fun bindsHintFragment(): HintFragment
}