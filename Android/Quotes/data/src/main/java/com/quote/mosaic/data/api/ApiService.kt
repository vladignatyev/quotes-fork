package com.quote.mosaic.data.api

import com.quote.mosaic.data.model.*
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
        @Header("Cache-Control") cacheControl: String?,
        @Path("id") id: Int
    ): Single<Response<ResponseData<TopicDO>>>

    @GET("profile/")
    fun profile(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<UserDO>>>

    @POST("category/{id}/unlock")
    fun openCategory(
        @Header("X-Client-Auth") token: String,
        @Path("id") id: Int
    ): Single<Response<Void>>

    @POST("level/{id}/complete")
    fun completeLevel(
        @Header("X-Client-Auth") token: String,
        @Path("id") id: Int
    ): Single<Response<Void>>

    @GET("levels/category/{id}/")
    fun categoryInfo(
        @Header("X-Client-Auth") token: String,
        @Path("id") id: Int
    ): Single<Response<ResponseData<QuoteDO>>>

}