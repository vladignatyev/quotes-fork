package com.quote.mosaic.ui.main.play.topup

import com.android.billingclient.api.SkuDetails

sealed class TopUpProductModel(
    open val id: String,
    open val billingProduct: SkuDetails,
    open val payload: String? = null
) {

    data class Item(
        override val id: String,
        override val billingProduct: SkuDetails,
        val title: String,
        val iconUrl: String,
        val price: String
    ) : TopUpProductModel(id, billingProduct)

    data class Featured(
        override val id: String,
        override val billingProduct: SkuDetails,
        val title: String,
        val iconUrl: String,
        val price: String
    ) : TopUpProductModel(id, billingProduct)

    data class Free(
        override val id: String,
        override val billingProduct: SkuDetails,
        override val payload: String? = null,
        val title: String,
        val iconUrl: String
    ) : TopUpProductModel(id, billingProduct, payload)

    data class Loading(
        override val id: String,
        override val billingProduct: SkuDetails
    ) : TopUpProductModel(id, billingProduct)
}