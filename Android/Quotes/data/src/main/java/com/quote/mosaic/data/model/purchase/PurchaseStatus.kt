package com.quote.mosaic.data.model.purchase

import com.fasterxml.jackson.annotation.JsonProperty

enum class PurchaseStatus {
    @JsonProperty("invalid")
    INVALID,
    @JsonProperty("unknown")
    UNKNOWN,
    @JsonProperty("cancelled")
    CANCELLED,
    @JsonProperty("purchased")
    PURCHASED
}