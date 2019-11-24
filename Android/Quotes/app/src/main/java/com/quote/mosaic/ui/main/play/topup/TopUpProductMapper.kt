package com.quote.mosaic.ui.main.play.topup

import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.core.billing.BillingProduct

interface TopUpProductMapper {

    fun toLocaleModel(bilingProducts: List<BillingProduct>): List<TopUpProductModel>

    fun toLoadingState(): List<TopUpProductModel>
}

class TopUpProductMapperImpl : TopUpProductMapper {

    override fun toLocaleModel(
        bilingProducts: List<BillingProduct>
    ): List<TopUpProductModel> = bilingProducts.mapNotNull { bilingProduct ->
        when (bilingProduct) {

            is BillingProduct.InApp -> {
                val remoteBro = bilingProduct.remoteBro
                if (remoteBro.isFeatured) {
                    TopUpProductModel.Featured(
                        id = remoteBro.id,
                        title = remoteBro.title,
                        iconUrl = remoteBro.imageUrl,
                        price = bilingProduct.skuDetails.price,
                        billingProduct = bilingProduct.skuDetails
                    )
                } else {
                    TopUpProductModel.Item(
                        id = remoteBro.id,
                        title = remoteBro.title,
                        iconUrl = remoteBro.imageUrl,
                        price = bilingProduct.skuDetails.price,
                        billingProduct = bilingProduct.skuDetails
                    )
                }
            }

            is BillingProduct.FreeCoins -> {
                TopUpProductModel.Free(
                    id = bilingProduct.remoteBro.id,
                    title = bilingProduct.remoteBro.title,
                    iconUrl = bilingProduct.remoteBro.imageUrl,
                    billingProduct = bilingProduct.skuDetails
                )
            }

            is BillingProduct.TestSku -> {
                TopUpProductModel.Free(
                    id = bilingProduct.remoteBro.id,
                    title = bilingProduct.remoteBro.title,
                    iconUrl = bilingProduct.remoteBro.imageUrl,
                    billingProduct = bilingProduct.skuDetails
                )
            }
            else -> null
        }
    }

    override fun toLoadingState(): List<TopUpProductModel> = listOf(
        TopUpProductModel.Loading("1", SkuDetails("{}")),
        TopUpProductModel.Loading("2", SkuDetails("{}")),
        TopUpProductModel.Loading("3", SkuDetails("{}")),
        TopUpProductModel.Loading("4", SkuDetails("{}")),
        TopUpProductModel.Loading("5", SkuDetails("{}"))
    )

}