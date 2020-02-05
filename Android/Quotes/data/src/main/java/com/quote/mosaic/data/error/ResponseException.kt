package com.quote.mosaic.data.error

import com.quote.mosaic.data.model.ErrorResponse

sealed class ResponseException : Exception() {
    data class Application(val error: ErrorResponse) : ResponseException()
    data class Network(val source: Throwable) : ResponseException()
    object NoConnectivity : ResponseException()
}