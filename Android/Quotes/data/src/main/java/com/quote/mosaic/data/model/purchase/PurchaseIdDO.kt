package com.quote.mosaic.data.model.purchase

import com.fasterxml.jackson.annotation.JsonProperty

data class PurchaseIdDO(
    @JsonProperty("purchase_id")
    val purchaseId: String
)