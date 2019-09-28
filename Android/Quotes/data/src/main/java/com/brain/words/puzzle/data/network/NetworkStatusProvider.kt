package com.brain.words.puzzle.data.network

import io.reactivex.Flowable

interface NetworkStatusProvider {
    fun isConnected(): Flowable<Boolean>
}