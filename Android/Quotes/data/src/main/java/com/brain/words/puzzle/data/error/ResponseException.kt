package com.brain.words.puzzle.data.error

import com.brain.words.puzzle.data.model.ErrorResponse

sealed class ResponseException : Exception() {
    data class Application(val error: ErrorResponse) : ResponseException()
    data class Network(val source: Throwable) : ResponseException()
    object NoConnectivity : ResponseException()
}