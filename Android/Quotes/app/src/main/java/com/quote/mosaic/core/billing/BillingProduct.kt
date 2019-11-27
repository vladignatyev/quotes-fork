package com.quote.mosaic.core.billing

import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.data.model.purchase.RechargeableSkuDO

data class BillingProduct(
    val skuDetails: SkuDetails,
    val remoteProduct: RemoteProduct
)