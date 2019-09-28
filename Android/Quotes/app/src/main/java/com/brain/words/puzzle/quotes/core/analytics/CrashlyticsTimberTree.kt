package com.brain.words.puzzle.quotes.core.analytics

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

class CrashlyticsTimberTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Crashlytics.log(priority, tag, message)
        Crashlytics.logException(t ?: Exception(message))
    }

    override fun isLoggable(tag: String?, priority: Int) = priority >= Log.WARN
}