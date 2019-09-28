package com.brain.words.puzzle.data.transform

import com.brain.words.puzzle.data.model.ErrorResponse
import com.brain.words.puzzle.data.error.ResponseErrorMessageExtractor
import com.brain.words.puzzle.data.error.ResponseException
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.SingleTransformer
import retrofit2.Response

class ResponseTransform<T>(
    private val isConnected: Boolean,
    private val errorExtractor: ResponseErrorMessageExtractor
) : SingleTransformer<Response<T>, T> {

    override fun apply(
        upstream: Single<Response<T>>
    ): SingleSource<T> = if (isConnected) {
        upstream.transform()
    } else {
        Single.error(ResponseException.NoConnectivity)
    }

    private fun Single<Response<T>>.transform() = onErrorResumeNext {
        Single.error(ResponseException.Network(it))
    }.flatMap {
        if (it.isSuccessful) {
            val responseBody = it.body()
            if (responseBody == null) {
                Single.error(Error("Response body is null, something is very wrong"))
            } else {
                Single.just(responseBody)
            }
        } else {
            val errors = errorExtractor.errors(it.errorBody())
            Single.error(ResponseException.Application(ErrorResponse(it.code(), errors)))
        }
    }
}