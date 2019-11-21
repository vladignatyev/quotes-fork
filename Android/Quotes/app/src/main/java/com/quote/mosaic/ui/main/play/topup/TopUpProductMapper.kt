package com.quote.mosaic.ui.main.play.topup

import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.data.model.AvailableProductsDO

interface TopUpProductMapper {

    fun toLocaleModel(
        remoteProducts: AvailableProductsDO,
        billingProducts: List<SkuDetails>
    ): List<TopUpProductModel>
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
                        "Парочка монет",
                        "1",
                        links.first(),
                        "Посмотри видео",
                        billingProducts.first()
                    )
                )
            } else {
                val remoteProduct = products[index]
                val billingProduct = billingProducts.first { it.sku == remoteProduct.sku }

                if (remoteProduct.isFeatured) {
                    localProducts.add(
                        TopUpProductModel.Featured(
                            id = remoteProduct.id,
                            title = remoteProduct.title.orEmpty(),
                            iconUrl = links[index + 1],
                            price = billingProduct.price,
                            billingProduct = billingProduct
                        )
                    )
                } else {
                    localProducts.add(
                        TopUpProductModel.Item(
                            id = remoteProduct.id,
                            title = remoteProduct.title.orEmpty(),
                            iconUrl = links[index + 1],
                            price = billingProduct.price,
                            billingProduct = billingProduct
                        )
                    )
                }
            }
        }
        return localProducts
    }

    private val links = listOf(
        "https://i.imgur.com/L9w8YwG.png",
        "https://i.imgur.com/afJDfKV.png",
        "https://i.imgur.com/GNYJNnb.png",
        "https://i.imgur.com/27dlZPi.png"
    )

}