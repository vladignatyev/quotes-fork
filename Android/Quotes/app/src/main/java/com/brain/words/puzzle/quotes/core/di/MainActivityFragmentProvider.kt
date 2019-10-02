package com.brain.words.puzzle.quotes.core.di

import com.brain.words.puzzle.quotes.ui.main.game.GameFragment
import com.brain.words.puzzle.quotes.ui.main.game.topic.TopicFragment
import com.brain.words.puzzle.quotes.ui.main.profile.ProfileFragment
import com.brain.words.puzzle.quotes.ui.main.top.TopFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityFragmentProvider {

    @ContributesAndroidInjector
    abstract fun bindsTopicFragment(): TopicFragment

    @ContributesAndroidInjector
    abstract fun bindsGameFragment(): GameFragment

    @ContributesAndroidInjector
    abstract fun bindsTopFragment(): TopFragment

    @ContributesAndroidInjector
    abstract fun bindsProfileFragment(): ProfileFragment
}