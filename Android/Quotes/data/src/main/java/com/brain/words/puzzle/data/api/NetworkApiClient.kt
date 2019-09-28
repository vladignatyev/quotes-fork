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

    override fun login(): Single<String> = apiService.login()
        .transform()

    private fun <T> Single<Response<T>>.transform() =
        compose(
            ResponseTransform(
                networkStatusProvider.isConnected().blockingFirst(),
                errorMessageExtractor
            )
        )
}