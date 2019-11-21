package com.quote.mosaic.ui.main.play.topup

import com.android.billingclient.api.SkuDetails

sealed class TopUpProductModel(
    open val billingProduct: SkuDetails
) {

    data class Item(
        val id: String,
        val title: String,
        val iconUrl: String,
        val price: String,
        override val billingProduct: SkuDetails
    ) : TopUpProductModel(billingProduct)

    data class Featured(
        val id: String,
        val title: String,
        val iconUrl: String,
        val price: String,
        override val billingProduct: SkuDetails
    ) : TopUpProductModel(billingProduct)

    data class Free(
        val id: String,
        val title: String,
        val iconUrl: String,
        val price: String,
        override val billingProduct: SkuDetails
    ) : TopUpProductModel(billingProduct)
}