package com.quote.mosaic.data.model.purchase

import com.fasterxml.jackson.annotation.JsonProperty

data class RechargeableSkuDO(
    val id: String,
    @JsonProperty("admin_title")
    val title: String,
    @JsonProperty("is_featured")
    val isFeatured: Boolean,
    val sku: String,
    @JsonProperty("balance_recharge")
    val balanceRecharge: Int?,
    @JsonProperty("is_rewarded")
    val isRewarded: Boolean,
    @JsonProperty("image_url")
    val imageUrl: String,
    val tags: List<RemoteProductTag>
)

enum class RemoteProductTag {
    @JsonProperty("topup")
    TOP_UP,
    @JsonProperty("hint_next_word")
    HINT_NEXT_WORD,
    @JsonProperty("doubleup")
    DOUBLE_UP
}