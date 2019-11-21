package com.quote.mosaic.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SkuDO(
    val id: String,
    @JsonProperty("admin_title")
    val title: String?,
    @JsonProperty("balance_recharge")
    val balanceRecharge: Int?,
    @JsonProperty("is_featured")
    val isFeatured: Boolean,
    val sku: String
)