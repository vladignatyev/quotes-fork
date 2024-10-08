package com.quote.mosaic.core

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.crashlytics.android.Crashlytics
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.MobileAds
import com.quote.mosaic.core.analytics.CrashlyticsTimberTree
import com.quote.mosaic.core.di.AppComponent
import com.quote.mosaic.core.di.AppDataBindingComponent
import com.quote.mosaic.core.di.DaggerAppComponent
import com.quote.mosaic.core.manager.FirebaseManager
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

    //Don't remove, needed to have up-to-date data on application start.
    @Inject
    lateinit var firebaseManager: FirebaseManager

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())

        Timber.plant(CrashlyticsTimberTree())

        MobileAds.initialize(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        setupDagger()

        setupFacebook()
    }

    private fun setupFacebook() {
        AppEventsLogger.activateApp(this)
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