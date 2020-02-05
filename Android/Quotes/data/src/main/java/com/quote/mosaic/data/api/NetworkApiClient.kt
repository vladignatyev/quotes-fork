package com.quote.mosaic.data.api

import com.quote.mosaic.data.error.ResponseErrorMessageExtractor
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.model.hints.HintsVariantsDO
import com.quote.mosaic.data.model.overview.MainTopicDO
import com.quote.mosaic.data.model.overview.QuoteDO
import com.quote.mosaic.data.model.overview.TopicDO
import com.quote.mosaic.data.model.purchase.AvailableProductsDO
import com.quote.mosaic.data.model.purchase.PurchaseIdDO
import com.quote.mosaic.data.model.purchase.PurchaseStatusDO
import com.quote.mosaic.data.model.user.UserDO
import com.quote.mosaic.data.network.NetworkStatusProvider
import com.quote.mosaic.data.transform.EmptyResponseTransform
import com.quote.mosaic.data.transform.ResponseTransform
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response

class NetworkApiClient(
    private val apiService: ApiService,
    private val errorMessageExtractor: ResponseErrorMessageExtractor,
    private val networkStatusProvider: NetworkStatusProvider,
    private val userManager: UserManager
) : ApiClient {

    // ---------------- USER ---------------- //
    override fun login(
        deviceId: String, timestamp: String, signature: String, nickname: String
    ): Single<String> {
        val body = HashMap<String, String>()
        body["device_token"] = deviceId
        body["timestamp"] = timestamp
        body["signature"] = signature
        body["nickname"] = nickname
        return apiService.login(body).transform().map { it.auth_token }
    }

    override fun profile(): Single<UserDO> =
        apiService.profile(userManager.getSession()).transform().map { it.objects.first() }

    override fun changeUserName(newName: String): Single<UserDO> {
        val session = userManager.getSession()

        val body = HashMap<String, String>()
        body["nickname"] = newName

        return apiService.changeUserName(session, body).transform().map { it.objects.first() }
    }

    override fun subscribePushNotifications(): Completable =
        if (!userManager.getDeviceToken().isNullOrEmpty()) {
            val body = HashMap<String, String>()
            body["token"] = userManager.getDeviceToken().orEmpty()
            apiService.subscribePushNotifications(userManager.getSession(), body)
                .transformToCompletable()
        } else {
            Completable.complete()
        }

    // ---------------- TOPICS ---------------- //
    override fun topics(): Single<List<MainTopicDO>> =
        apiService.topics(userManager.getSession()).transform().map { it.objects.requireNoNulls() }

    override fun topic(id: Int, force: Boolean): Single<TopicDO> =
        apiService.topic(
            userManager.getSession(), force.toCacheHeader(), id
        ).transform().map { it.objects.first() }

    // ---------------- CATEGORIES ---------------- //
    override fun openCategory(id: Int): Completable =
        apiService.openCategory(userManager.getSession(), id).transformToCompletable()

    override fun quotesList(id: Int): Single<List<QuoteDO>> =
        apiService.categoryInfo(
            userManager.getSession(), id
        ).transform().map { it.objects.requireNoNulls() }

    override fun completeLevel(id: Int): Completable =
        apiService.completeLevel(userManager.getSession(), id).transformToCompletable()

    // ---------------- PURCHASES ---------------- //
    override fun getSkuList(): Single<AvailableProductsDO> =
        apiService.getSkuList(userManager.getSession()).transform().map { it.objects.first() }

    override fun getPurchaseStatus(token: String): Single<PurchaseStatusDO> =
        apiService.getPurchaseStatus(
            userManager.getSession(), token
        ).transform().map { it.objects.first() }

    override fun registerPurchase(
        orderId: String,
        purchaseToken: String,
        appProduct: String,
        payload: String?
    ): Single<PurchaseIdDO> {
        val body = HashMap<String, String>()
        body["order_id"] = orderId
        body["purchase_token"] = purchaseToken
        body["app_product"] = appProduct
        payload?.let {
            body["payload"] = it
        }

        return apiService.registerPurchase(userManager.getSession(), body).transform()
            .map { it.objects.first() }
    }

    // ---------------- HINTS ----------------- //
    override fun getHints(): Single<HintsVariantsDO> =
        apiService.getHintsList(userManager.getSession()).transform().map { it.objects.first() }

    override fun validateHint(hintId: String, levelId: String): Completable {
        val body = HashMap<String, String>()
        body["coin_product"] = hintId
        body["payload"] = levelId

        return apiService.validateHint(userManager.getSession(), body).transformToCompletable()
    }

    // ---------------- STATS ----------------- //
    override fun globalTop(): Single<String> =
        apiService.globalTop(userManager.getSession()).transform()
            .map { it.objects.first() }

    override fun globalAchievements(): Single<String> =
        apiService.globalAchievements(userManager.getSession()).transform()
            .map { it.objects.first() }

    override fun userAchievements(): Single<String> =
        apiService.userAchievements(userManager.getSession()).transform()
            .map { it.objects.first() }

    // ---------------- COMMON ---------------- //
    private fun <T> Single<Response<T>>.transform() =
        compose(
            ResponseTransform(
                networkStatusProvider.isConnected().blockingFirst(),
                errorMessageExtractor
            )
        )

    private fun Single<Response<Void>>.transformToCompletable() = compose(
        EmptyResponseTransform(
            networkStatusProvider.isConnected().blockingFirst(),
            errorMessageExtractor
        )
    ).ignoreElement()

    private fun Boolean.toCacheHeader(): String? = if (this) "no-cache" else null

}