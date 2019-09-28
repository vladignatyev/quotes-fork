package com.brain.words.puzzle.data.model

data class ErrorResponse(
    val errorCode: Int,
    val errors: List<ErrorDO>
) {
    fun unauthorized() = errorCode == 401
    fun notFound() = errorCode == 404
}

data class ErrorDO(
    val code: Int, val field: String, val message: String
) {
    fun root() = field == "root"
}