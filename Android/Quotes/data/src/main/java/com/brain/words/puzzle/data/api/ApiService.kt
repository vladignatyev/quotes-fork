package com.brain.words.puzzle.data.api

import com.brain.words.puzzle.data.model.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/")
    fun login(
        @Body body: HashMap<String, String>
    ): Single<Response<TokenDO>>

    @GET("topic/list/")
    fun topics(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<MainTopicDO>>>

    @GET("topic/{id}/")
    fun topic(
        @Header("X-Client-Auth") token: String,
        @Path("id") id: Int
    ): Single<Response<ResponseData<TopicDO>>>

    @GET("profile/")
    fun profile(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<UserDO>>>

}