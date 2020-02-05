package com.quote.mosaic.core.billing

import com.quote.mosaic.data.model.purchase.RemoteProductTag

data class RemoteProduct(
    val id: String,
    val title: String,
    val isFeatured: Boolean,
    val isRewarded: Boolean,
    val balanceRecharge: Int,
    val sku: String,
    val testSku: String,
    val imageUrl: String,
    val tags: List<RemoteProductTag>
)