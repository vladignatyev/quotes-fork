package com.quote.mosaic.data.model.purchase

import com.fasterxml.jackson.annotation.JsonProperty

data class RechargeableSkuDO(
    val id: String,
    @JsonProperty("admin_title")
    val title: String,
    @JsonProperty("balance_recharge")
    val balanceRecharge: Int,
    @JsonProperty("is_featured")
    val isFeatured: Boolean,
    @JsonProperty("image_url")
    val imageUrl: String,
    val sku: String
)