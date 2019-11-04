package com.brain.words.puzzle.data.api

import com.brain.words.puzzle.data.UserManager
import com.brain.words.puzzle.data.error.ResponseErrorMessageExtractor
import com.brain.words.puzzle.data.model.MainTopicDO
import com.brain.words.puzzle.data.model.TopicDO
import com.brain.words.puzzle.data.model.UserDO
import com.brain.words.puzzle.data.network.NetworkStatusProvider
import com.brain.words.puzzle.data.transform.ResponseTransform
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

    private fun <T> Single<Response<T>>.transform() =
        compose(
            ResponseTransform(
                networkStatusProvider.isConnected().blockingFirst(),
                errorMessageExtractor
            )
        )
}