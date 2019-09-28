package com.brain.words.puzzle.data.api

import io.reactivex.Single

interface ApiClient {

    fun login(): Single<String>
}