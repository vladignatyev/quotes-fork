package com.quote.mosaic.core

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class AppViewModel : ViewModel() {

    private var isInitialised = false

    private val disposeOnClear = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposeOnClear.dispose()
    }

    fun Disposable.untilCleared() = also { disposeOnClear.add(it) }

    fun init() {
        if (!isInitialised) {
            initialise()
        }
        isInitialised = true
    }

    protected open fun initialise() {
        // Overridden by children which require one-time initialization upon construction but for
        // which init {} block is too early.
    }
}