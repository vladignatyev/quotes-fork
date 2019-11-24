package com.quote.mosaic.core.billing

sealed class BillingManagerResult {
    object Loading : BillingManagerResult()
    object Success : BillingManagerResult()
    data class Retry(val cause: String) : BillingManagerResult()
}