package com.quote.mosaic.core

import io.reactivex.Scheduler

interface Schedulers {
    fun ui(): Scheduler
    fun cpu(): Scheduler
    fun io(): Scheduler
}