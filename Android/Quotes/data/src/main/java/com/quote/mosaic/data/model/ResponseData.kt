package com.quote.mosaic.data.model

data class ResponseData<out T>(
    val objects: List<T?>
)