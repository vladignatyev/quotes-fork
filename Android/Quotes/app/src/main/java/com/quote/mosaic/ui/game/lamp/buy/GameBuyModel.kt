package com.quote.mosaic.ui.game.lamp.buy

import com.android.billingclient.api.SkuDetails

sealed class GameBuyModel {

    data class WatchVideo(val title: String, val count: String) : GameBuyModel()

    data class PurchaseCoins(
        val productId: String,
        val title: String,
        val count: Int,
        val price: String,
        val description: String,
        val rawJson: SkuDetails?
    ) : GameBuyModel()
}