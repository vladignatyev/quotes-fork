package com.quote.mosaic.core

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.quote.mosaic.BuildConfig
import com.quote.mosaic.core.analytics.CrashlyticsTimberTree
import com.quote.mosaic.core.di.AppComponent
import com.quote.mosaic.core.di.DaggerAppComponent
import com.quote.mosaic.core.di.AppDataBindingComponent
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