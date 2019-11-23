package com.quote.mosaic.data.billing

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface BillingManager {

    fun start(): Completable

    fun getBuyVariants(skuList: List<String>): Single<List<SkuDetails>>

    fun allowMultiplePurchases(): Single<Purchase>

    fun launchBuyWorkFlow(activity: Activity, skuDetails: SkuDetails): Completable

    fun subscribePurchaseUpdates(): Observable<Int>

    fun clearHistory(): Completable

}