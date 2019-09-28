package com.brain.words.puzzle.quotes.core

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

object PlatformSchedulers : Schedulers {
    override fun ui(): Scheduler = AndroidSchedulers.mainThread()

    override fun cpu(): Scheduler = io.reactivex.schedulers.Schedulers.computation()

    override fun io(): Scheduler = io.reactivex.schedulers.Schedulers.io()
}