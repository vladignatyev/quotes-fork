package com.brain.words.puzzle.data.api

import com.brain.words.puzzle.data.model.TokenDO
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/")
    fun login(
        @Body body: HashMap<String, String>
    ): Single<Response<TokenDO>>

}