package com.quote.mosaic.data.api

import com.quote.mosaic.data.model.hints.HintsVariantsDO
import com.quote.mosaic.data.model.overview.MainTopicDO
import com.quote.mosaic.data.model.overview.QuoteDO
import com.quote.mosaic.data.model.overview.TopicDO
import com.quote.mosaic.data.model.purchase.AvailableProductsDO
import com.quote.mosaic.data.model.purchase.PurchaseIdDO
import com.quote.mosaic.data.model.purchase.PurchaseStatusDO
import com.quote.mosaic.data.model.user.UserDO
import io.reactivex.Completable
import io.reactivex.Single

interface ApiClient {

    // User
    fun login(
        deviceId: String, timestamp: String, signature: String, nickname: String
    ): Single<String>

    fun profile(): Single<UserDO>

    fun changeUserName(newName: String): Single<UserDO>

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

    fun getPurchaseStatus(token: String): Single<PurchaseStatusDO>

    fun registerPurchase(orderId: String, purchaseToken: String, appProduct: String, payload: String?): Single<PurchaseIdDO>

    // Hints
    fun getHints(): Single<HintsVariantsDO>

    fun validateHint(hintId: String, levelId: String): Completable

    // Stats
    fun globalTop(): Single<String>

    fun globalAchievements(): Single<String>

    fun userAchievements(): Single<String>

}