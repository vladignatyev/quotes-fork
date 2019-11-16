package com.quote.mosaic.data.transform

import com.quote.mosaic.data.error.ResponseErrorMessageExtractor
import com.quote.mosaic.data.error.ResponseException
import com.quote.mosaic.data.model.ErrorResponse
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.SingleTransformer
import retrofit2.Response

class EmptyResponseTransform(
    private val isConnected: Boolean,
    private val errorExtractor: ResponseErrorMessageExtractor
)
    : SingleTransformer<Response<Void>, Unit> {
    override fun apply(
        upstream: Single<Response<Void>>
    ): SingleSource<Unit> = if (isConnected) {
        upstream.transform()
    } else {
        Single.error(ResponseException.NoConnectivity)
    }

    private fun Single<Response<Void>>.transform(): Single<Unit> = onErrorResumeNext {
        Single.error(ResponseException.Network(it))
    }.flatMap {
        if (it.isSuccessful) {
            Single.just(Unit)
        } else {
            Single.error(ResponseException.Application(ErrorResponse(it.code())))
        }
    }
}
