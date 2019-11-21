package com.quote.mosaic.data.api

import com.quote.mosaic.data.model.*
import io.reactivex.Completable
import io.reactivex.Single

interface ApiClient {

    // User
    fun login(
        deviceId: String, timestamp: String, signature: String, nickname: String
    ): Single<String>

    fun profile(): Single<UserDO>

    fun subscribePushNotifications(): Completable

    // Topics
    fun topics(): Single<List<MainTopicDO>>

    fun topic(id: Int, force: Boolean = false): Single<TopicDO>

    // Categories
    fun openCategory(id: Int): Completable

    fun quotesList(id: Int): Single<List<QuoteDO>>

    fun completeLevel(id: Int): Completable

    // Purchases
    fun getSkuList(): Single<AvailableProductsDO>

}