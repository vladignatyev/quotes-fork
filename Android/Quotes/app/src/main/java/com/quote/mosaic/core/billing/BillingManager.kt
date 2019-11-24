package com.quote.mosaic.core.billing

import android.app.Activity
import com.quote.mosaic.ui.main.play.topup.TopUpProductModel
import io.reactivex.Completable
import io.reactivex.Flowable

interface BillingManager {

    fun warmUp()

    fun availableSkus(): List<BillingProduct>

    fun launchBuyWorkFlow(activity: Activity, product: TopUpProductModel): Completable

    fun billingResultTrigger(): Flowable<BillingManagerResult>

}