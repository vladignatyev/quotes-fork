package com.quote.mosaic.ui.main.play.topup

import com.quote.mosaic.core.billing.BillingProduct

interface TopUpProductMapper {

    fun toLocaleModel(billingProducts: List<BillingProduct>): List<TopUpProductModel>

}

class TopUpProductMapperImpl : TopUpProductMapper {

    override fun toLocaleModel(
        billingProducts: List<BillingProduct>
    ): List<TopUpProductModel> = billingProducts.mapNotNull { billingProduct ->
        val remoteBro = billingProduct.remoteProduct

        if (remoteBro.isRewarded) {
            TopUpProductModel.Free(
                id = remoteBro.id,
                title = remoteBro.title,
                iconUrl = remoteBro.imageUrl,
                billingProduct = billingProduct.skuDetails
            )
        } else {
            if (remoteBro.isFeatured) {
                TopUpProductModel.Featured(
                    id = remoteBro.id,
                    title = remoteBro.title,
                    iconUrl = remoteBro.imageUrl,
                    price = billingProduct.skuDetails.price,
                    billingProduct = billingProduct.skuDetails
                )
            } else {
                TopUpProductModel.Item(
                    id = remoteBro.id,
                    title = remoteBro.title,
                    iconUrl = remoteBro.imageUrl,
                    price = billingProduct.skuDetails.price,
                    billingProduct = billingProduct.skuDetails
                )
            }
        }
    }
}