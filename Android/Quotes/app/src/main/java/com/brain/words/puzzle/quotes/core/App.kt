package com.brain.words.puzzle.quotes.core

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.brain.words.puzzle.quotes.BuildConfig
import com.brain.words.puzzle.quotes.core.analytics.CrashlyticsTimberTree
import com.brain.words.puzzle.quotes.core.di.AppComponent
import com.brain.words.puzzle.quotes.core.di.DaggerAppComponent
import com.brain.words.puzzle.quotes.core.di.AppDataBindingComponent
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasAndroidInjector {

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var appDataBindingComponent: AppDataBindingComponent

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTimberTree())
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        setupDagger()
    }

    private fun setupDagger() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .context(this)
            .build()

        appComponent.inject(this)

        DataBindingUtil.setDefaultComponent(appDataBindingComponent)
    }

    override fun androidInjector(): AndroidInjector<Any> = activityDispatchingAndroidInjector

}