package com.quote.mosaic.ui.main.play.topup

import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.data.model.purchase.AvailableProductsDO

interface TopUpProductMapper {

    fun toLocaleModel(
        remoteProducts: AvailableProductsDO,
        billingProducts: List<SkuDetails>
    ): List<TopUpProductModel>

    fun toLoadingState(): List<TopUpProductModel>
}

class TopUpProductMapperImpl : TopUpProductMapper {

    override fun toLocaleModel(
        remoteProducts: AvailableProductsDO,
        billingProducts: List<SkuDetails>
    ): List<TopUpProductModel> {

        val products = remoteProducts.rechargeable
        val localProducts = mutableListOf<TopUpProductModel>()

        for (index in 0..products.size) {

            if (index == products.size) {
                localProducts.add(
                    TopUpProductModel.Free(
                        id = "Парочка монет",
                        title = "1",
                        iconUrl = "https://i.imgur.com/L9w8YwG.png",
                        price = "Посмотри видео",
                        billingProduct = billingProducts.first()
                    )
                )
            } else {
                val remoteProduct = products[index]
                val billingProduct = billingProducts.first { it.sku == remoteProduct.sku }

                if (remoteProduct.isFeatured) {
                    localProducts.add(
                        TopUpProductModel.Featured(
                            id = remoteProduct.id,
                            title = remoteProduct.title,
                            iconUrl = remoteProduct.imageUrl,
                            price = billingProduct.price,
                            billingProduct = billingProduct
                        )
                    )
                } else {
                    localProducts.add(
                        TopUpProductModel.Item(
                            id = remoteProduct.id,
                            title = remoteProduct.title,
                            iconUrl = remoteProduct.imageUrl,
                            price = billingProduct.price,
                            billingProduct = billingProduct
                        )
                    )
                }
            }
        }
        return localProducts
    }

    override fun toLoadingState(): List<TopUpProductModel> = listOf(
        TopUpProductModel.Loading("1", SkuDetails("{}")),
        TopUpProductModel.Loading("2", SkuDetails("{}")),
        TopUpProductModel.Loading("3", SkuDetails("{}")),
        TopUpProductModel.Loading("4", SkuDetails("{}")),
        TopUpProductModel.Loading("5", SkuDetails("{}"))
    )

}