package com.quote.mosaic.data.manager

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BillingManagerImpl(
    private val context: Context
) : PurchasesUpdatedListener, BillingManager {

    private var billingClient: BillingClient? = null

    private var purchases: MutableList<Purchase>? = null

    private val purchasesUpdatesRelay = BehaviorRelay.createDefault(-100)

    override fun subscribePurchaseUpdates(): Observable<Int> = purchasesUpdatesRelay.hide()

    override fun start(): Completable = Completable
        .unsafeCreate { emitter ->
            if (billingClient == null) {
                billingClient = BillingClient
                    .newBuilder(context)
                    .setListener(this)
                    .enablePendingPurchases()
                    .build()
            }
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    emitter.onError(Throwable("BILLING | onBillingServiceDisconnected | DISCONNECTED"))
                }

                override fun onBillingSetupFinished(billingResult: BillingResult?) {
                    if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                        println("BILLING | startConnection | RESULT OK")
                        emitter.onComplete()
                    } else {
                        emitter.onError(Throwable("Billing startConnection failed with code: ${billingResult?.responseCode}"))
                    }
                }
            })
        }

    override fun getBuyVariants(skuList: List<String>): Single<List<SkuDetails>> = Single
        .create { emitter ->
            val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            billingClient?.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                println("querySkuDetailsAsync, responseCode: $responseCode")
                if (responseCode.responseCode == BillingClient.BillingResponseCode.OK) {
                    emitter.onSuccess(skuDetailsList)
                } else {
                    emitter.onError(BillingError(responseCode.responseCode))
                    Timber.e("Can't querySkuDetailsAsync, responseCode: $responseCode")
                }
            }
        }

    override fun allowMultiplePurchases(): Single<Purchase> = Single
        .create { emitter ->
            val purchase = purchases?.first()
            if (purchase != null) {

                val params = ConsumeParams
                    .newBuilder()
                    .setDeveloperPayload(purchase.developerPayload)
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.consumeAsync(params) { result, purchaseToken ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
                        emitter.onSuccess(purchase)
                    } else {
                        println("Can't allowMultiplePurchases, responseCode: ${result.responseCode}")
                        emitter.onError(BillingError(result.responseCode))
                    }
                }
            } else {
                emitter.onError(BillingError(-6699))
            }
        }

    override fun launchBuyWorkFlow(activity: Activity, skuDetails: SkuDetails): Completable =
        Completable.fromCallable {
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            billingClient?.launchBillingFlow(activity, billingFlowParams)
        }

    override fun clearHistory(): Completable = Completable.fromAction {
        billingClient
            ?.queryPurchases(BillingClient.SkuType.INAPP)
            ?.purchasesList
            ?.forEach { purchase ->
                val params = ConsumeParams
                    .newBuilder()
                    .setDeveloperPayload(purchase.developerPayload)
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.consumeAsync(params) { response, purchaseToken ->
                    if (response.responseCode == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
                        println("onPurchases Updated consumeAsync, purchases token dealed: $purchaseToken")
                    } else {
                        println("onPurchases some troubles happened: ${response.responseCode}")
                    }
                }
            }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        this.purchases = purchases
        purchasesUpdatesRelay.accept(billingResult?.responseCode)
    }
}