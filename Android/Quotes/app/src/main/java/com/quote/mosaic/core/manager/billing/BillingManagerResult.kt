package com.quote.mosaic.core.manager.billing

sealed class BillingManagerResult {
    object Loading : BillingManagerResult()
    object Success : BillingManagerResult()
    data class Retry(val cause: String) : BillingManagerResult()
}

class BillingError(val errorCode: Int) : Throwable() {

}