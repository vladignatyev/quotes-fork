package com.quote.mosaic.data.model

data class QuoteDO(
    val id: Int,
    val text: String,
    val author: String,
    val reward: Int,
    val beautiful: String,
    val complete: Boolean,
    val splitted: List<String>
)