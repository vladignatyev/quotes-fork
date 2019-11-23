package com.quote.mosaic.data.model.purchase

import com.fasterxml.jackson.annotation.JsonProperty

data class AvailableProductsDO(
    @JsonProperty("recharge_products")
    val rechargeable: List<RechargeableSkuDO>,
    @JsonProperty("other_products")
    val others: List<OthersSkuDO>
)