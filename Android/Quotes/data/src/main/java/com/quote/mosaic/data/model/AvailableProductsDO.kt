package com.quote.mosaic.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AvailableProductsDO(
    @JsonProperty("recharge_products")
    val rechargeable: List<SkuDO>,
    @JsonProperty("other_products")
    val others: List<SkuDO>
)