package com.quote.mosaic.data.model.purchase

import com.fasterxml.jackson.annotation.JsonProperty

data class AvailableProductsDO(
    @JsonProperty("balance_recharge")
    val rechargeable: List<RechargeableSkuDO>,
    @JsonProperty("doubleup")
    val doubleUp: List<RechargeableSkuDO>,
    @JsonProperty("test_sku")
    val testSku: String
)