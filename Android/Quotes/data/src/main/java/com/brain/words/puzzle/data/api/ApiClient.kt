package com.brain.words.puzzle.data.api

import io.reactivex.Single

interface ApiClient {

    fun login(
        deviceId: String, timestamp: String, signature: String, nickname: String
    ): Single<String>
}