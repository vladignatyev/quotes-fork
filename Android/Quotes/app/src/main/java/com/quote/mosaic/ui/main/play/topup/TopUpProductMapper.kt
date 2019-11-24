package com.quote.mosaic.ui.main.play.topup

import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.data.model.purchase.AvailableProductsDO
import com.quote.mosaic.data.model.purchase.isVideo

interface TopUpProductMapper {

    fun toLocaleModel(
        remoteProducts: AvailableProductsDO, billingProducts: List<SkuDetails>
    ): List<TopUpProductModel>

    fun toLoadingState(): List<TopUpProductModel>
}

class TopUpProductMapperImpl : TopUpProductMapper {

    override fun toLocaleModel(
        remoteProducts: AvailableProductsDO,
        billingProducts: List<SkuDetails>
    ): List<TopUpProductModel> = billingProducts.map { billingProduct ->
        val remoteProduct = remoteProducts.rechargeable.first { it.sku == billingProduct.sku }
        when {
            remoteProduct.isVideo() -> TopUpProductModel.Free(
                id = remoteProduct.id,
                title = remoteProduct.title,
                iconUrl = remoteProduct.imageUrl,
                price = billingProduct.price,
                billingProduct = billingProduct
            )

            remoteProduct.isFeatured -> TopUpProductModel.Featured(
                id = remoteProduct.id,
                title = remoteProduct.title,
                iconUrl = remoteProduct.imageUrl,
                price = billingProduct.price,
                billingProduct = billingProduct
            )

            else -> TopUpProductModel.Item(
                id = remoteProduct.id,
                title = remoteProduct.title,
                iconUrl = remoteProduct.imageUrl,
                price = billingProduct.price,
                billingProduct = billingProduct
            )
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