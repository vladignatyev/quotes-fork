package com.quote.mosaic.core.billing

import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.data.model.purchase.RechargeableSkuDO

sealed class BillingProduct(
    open val skuDetails: SkuDetails
) {

    /**
     *  General "In app" for real money
     * */
    data class InApp(
        override val skuDetails: SkuDetails,
        val remoteBro: RechargeableSkuDO
    ) : BillingProduct(skuDetails)

    /**
     *  Watch video to get coins double up after success level
     * */
    data class DoubleUp(
        override val skuDetails: SkuDetails,
        val remoteBro: RechargeableSkuDO
    ) : BillingProduct(skuDetails)

    /**
     *  Watch video to get Free Coins
     * */
    data class FreeCoins(
        override val skuDetails: SkuDetails,
        val remoteBro: RechargeableSkuDO
    ) : BillingProduct(skuDetails)

    /**
     *  Watch video to get hint "next word"
     * */
    data class NextWord(
        override val skuDetails: SkuDetails,
        val remoteBro: RechargeableSkuDO
    ) : BillingProduct(skuDetails)

    /**
     * General sku for testing
     * */
    data class TestSku(
        override val skuDetails: SkuDetails,
        val remoteBro: RechargeableSkuDO
    ) : BillingProduct(skuDetails)
}