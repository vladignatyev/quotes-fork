package com.brain.words.puzzle.quotes.core

import io.reactivex.Scheduler

interface Schedulers {
    fun ui(): Scheduler
    fun cpu(): Scheduler
    fun io(): Scheduler
}