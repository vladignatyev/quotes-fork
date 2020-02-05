package com.quote.mosaic.data.network

import io.reactivex.Flowable

interface NetworkStatusProvider {
    fun isConnected(): Flowable<Boolean>
}