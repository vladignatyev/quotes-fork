package com.brain.words.puzzle.data.api

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.POST

interface ApiService {

    @POST("user/login")
    fun login(): Single<Response<String>>

}