package com.quote.mosaic.data.model

data class ErrorResponse(
    val errorCode: Int
) {
    fun unauthorized() = errorCode == 401
    fun notFound() = errorCode == 404
    fun lowBalance() = errorCode == 402
}

data class ErrorDO(
    val code: Int, val field: String, val message: String
) {
    fun root() = field == "root"
}