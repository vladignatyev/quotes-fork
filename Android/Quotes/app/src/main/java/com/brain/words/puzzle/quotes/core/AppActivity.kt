package com.brain.words.puzzle.quotes.core

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign

/**
 * Base class for application's activities.
 */
abstract class AppActivity : AppCompatActivity() {

    private val disposeOnStop = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
    }

    protected fun Disposable.untilStopped(): Disposable {
        disposeOnStop += this
        return this
    }

    @CallSuper
    override fun onStop() {
        disposeOnStop.clear()
        super.onStop()
    }
}