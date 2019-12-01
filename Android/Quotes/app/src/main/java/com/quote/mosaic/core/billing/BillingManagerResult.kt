package com.quote.mosaic.core.billing

sealed class BillingManagerResult {
    object Loading : BillingManagerResult()
    data class Success(val sku: String?) : BillingManagerResult()
    data class Retry(val sku: String?, val cause: String) : BillingManagerResult()
}