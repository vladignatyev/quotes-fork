package com.quote.mosaic.data.api

import com.quote.mosaic.data.UserManager
import com.quote.mosaic.data.error.ResponseErrorMessageExtractor
import com.quote.mosaic.data.model.MainTopicDO
import com.quote.mosaic.data.model.QuoteDO
import com.quote.mosaic.data.model.TopicDO
import com.quote.mosaic.data.model.UserDO
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

    override fun topics(): Single<List<MainTopicDO>> =
        apiService.topics(userManager.getSession()).transform().map { it.objects.requireNoNulls() }

    override fun topic(id: Int): Single<TopicDO> =
        apiService.topic(userManager.getSession(), id).transform().map { it.objects.first() }

    override fun openCategory(id: Int): Completable =
        apiService.openCategory(userManager.getSession(), id).transformToCompletable()

    override fun quotesList(id: Int): Single<List<QuoteDO>> =
        apiService.categoryInfo(
            userManager.getSession(),
            id
        ).transform().map { it.objects.requireNoNulls() }

    override fun completeLevel(id: Int): Completable =
        apiService.completeLevel(userManager.getSession(), id).transformToCompletable()

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
}