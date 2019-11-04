package com.brain.words.puzzle.data.model

data class ResponseData<out T>(
    val objects: List<T?>
)