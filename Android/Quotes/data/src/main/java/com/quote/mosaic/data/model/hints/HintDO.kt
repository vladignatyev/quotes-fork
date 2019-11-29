package com.quote.mosaic.data.model.hints

import com.fasterxml.jackson.annotation.JsonProperty

data class HintDO(
    val id: String,
    @JsonProperty("admin_title")
    val title: String,
    @JsonProperty("is_featured")
    val isFeatured: Boolean,
    @JsonProperty("image_url")
    val imageUrl: String,
    @JsonProperty("coin_price")
    val coinPrice: Int
)