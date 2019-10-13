package com.brain.words.puzzle.data.api

import com.brain.words.puzzle.data.error.ResponseErrorMessageExtractor
import com.brain.words.puzzle.data.network.NetworkStatusProvider
import com.brain.words.puzzle.data.transform.ResponseTransform
import io.reactivex.Single
import retrofit2.Response

class NetworkApiClient(
    private val apiService: ApiService,
    private val errorMessageExtractor: ResponseErrorMessageExtractor,
    private val networkStatusProvider: NetworkStatusProvider
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

    private fun <T> Single<Response<T>>.transform() =
        compose(
            ResponseTransform(
                networkStatusProvider.isConnected().blockingFirst(),
                errorMessageExtractor
            )
        )
}