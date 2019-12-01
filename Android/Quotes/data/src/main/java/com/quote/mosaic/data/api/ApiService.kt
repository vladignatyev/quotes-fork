package com.quote.mosaic.data.api

import com.quote.mosaic.data.model.ResponseData
import com.quote.mosaic.data.model.hints.HintsVariantsDO
import com.quote.mosaic.data.model.overview.MainTopicDO
import com.quote.mosaic.data.model.overview.QuoteDO
import com.quote.mosaic.data.model.overview.TopicDO
import com.quote.mosaic.data.model.purchase.AvailableProductsDO
import com.quote.mosaic.data.model.purchase.PurchaseIdDO
import com.quote.mosaic.data.model.purchase.PurchaseStatusDO
import com.quote.mosaic.data.model.user.TokenDO
import com.quote.mosaic.data.model.user.UserDO
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------------- USER ---------------- //
    @POST("auth/")
    fun login(
        @Body body: HashMap<String, String>
    ): Single<Response<TokenDO>>

    @GET("profile/")
    @Headers("Cache-Control: no-cache")
    fun profile(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<UserDO>>>

    @POST("profile/update/")
    fun changeUserName(
        @Header("X-Client-Auth") token: String,
        @Body body: HashMap<String, String>
    ): Single<Response<ResponseData<UserDO>>>

    @POST("notifications/subscribe/")
    fun subscribePushNotifications(
        @Header("X-Client-Auth") token: String,
        @Body body: HashMap<String, String>
    ): Single<Response<Void>>

    // ---------------- TOPICS ---------------- //
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

    // ---------------- CATEGORIES ---------------- //
    @POST("category/{id}/unlock")
    fun openCategory(
        @Header("X-Client-Auth") token: String,
        @Path("id") id: Int
    ): Single<Response<Void>>

    @POST("level/{id}/complete")
    @Headers("Cache-Control: no-cache")
    fun completeLevel(
        @Header("X-Client-Auth") token: String,
        @Path("id") id: Int
    ): Single<Response<Void>>

    @GET("levels/category/{id}/")
    fun categoryInfo(
        @Header("X-Client-Auth") token: String,
        @Path("id") id: Int
    ): Single<Response<ResponseData<QuoteDO>>>

    // ---------------- PURCHASES ---------------- //
    @GET("purchase/play/products/")
    @Headers("Cache-Control: no-cache")
    fun getSkuList(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<AvailableProductsDO>>>

    @GET("purchase/play/status/{purchaseToken}/")
    @Headers("Cache-Control: no-cache")
    fun getPurchaseStatus(
        @Header("X-Client-Auth") token: String,
        @Path("purchaseToken") purchaseToken: String
    ): Single<Response<ResponseData<PurchaseStatusDO>>>

    @POST("purchase/play/")
    @Headers("Cache-Control: no-cache")
    fun registerPurchase(
        @Header("X-Client-Auth") token: String,
        @Body body: HashMap<String, String>
    ): Single<Response<ResponseData<PurchaseIdDO>>>

    // ---------------- HINTS ---------------- //
    @GET("coin/products/")
    @Headers("Cache-Control: no-cache")
    fun getHintsList(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<HintsVariantsDO>>>

    @POST("coin/consume/")
    @Headers("Cache-Control: no-cache")
    fun validateHint(
        @Header("X-Client-Auth") token: String,
        @Body body: HashMap<String, String>
    ): Single<Response<Void>>

    // ---------------- STATS ---------------- //
    @GET("quoterank/")
    @Headers("Cache-Control: no-cache")
    fun globalTop(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<String>>>

    @GET("achievements/all/")
    @Headers("Cache-Control: no-cache")
    fun globalAchievements(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<String>>>

    @GET("achievements/")
    @Headers("Cache-Control: no-cache")
    fun userAchievements(
        @Header("X-Client-Auth") token: String
    ): Single<Response<ResponseData<String>>>

}