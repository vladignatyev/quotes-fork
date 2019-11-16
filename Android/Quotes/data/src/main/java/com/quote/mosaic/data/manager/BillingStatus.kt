package com.quote.mosaic.data.manager

enum class BillingStatus {
    CONNECTED,
    UNWANTEDCONNECTION,
    DISCONNECTED,
    UNKNOWN
}

class BillingError(val errorCode: Int) : Throwable() {

}