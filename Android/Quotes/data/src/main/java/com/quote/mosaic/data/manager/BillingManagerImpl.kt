package com.quote.mosaic.data.manager

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

class BillingManagerImpl(
    private val context: Context
) : PurchasesUpdatedListener, BillingManager {

    private var billingClient: BillingClient? = null

    private var purchases: MutableList<Purchase>? = null

    private val purchasesUpdatesRelay = BehaviorRelay.createDefault<Int>(-100)

    private val billingStatusRelay = BehaviorRelay.createDefault<BillingStatus>(BillingStatus.UNKNOWN)

    override fun subscribePurchaseUpdates(): Observable<Int> = purchasesUpdatesRelay.hide()

    override fun subscribeConnectionStatus(): Observable<BillingStatus> = billingStatusRelay.hide()

    override fun start(): Completable = Completable
        .fromAction {
            if (billingClient == null) {
                billingClient = BillingClient
                    .newBuilder(context)
                    .setListener(this)
                    .build()
            }
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                    if (billingResponseCode == BillingClient.BillingResponse.OK) {
                        billingStatusRelay.accept(BillingStatus.CONNECTED)
                        println("BILLING | startConnection | RESULT OK")
                    } else {
                        billingStatusRelay.accept(BillingStatus.UNWANTEDCONNECTION)
                        println("BILLING | startConnection | RESULT: $billingResponseCode")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    billingStatusRelay.accept(BillingStatus.DISCONNECTED)
                    println("BILLING | onBillingServiceDisconnected | DISCONNECTED")
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
                if (responseCode == BillingClient.BillingResponse.OK) {
                    emitter.onSuccess(skuDetailsList)
                } else {
                    emitter.onError(BillingError(responseCode))
                    Timber.e("Can't querySkuDetailsAsync, responseCode: $responseCode")
                }
            }
        }

    override fun allowMultiplePurchases(): Single<Purchase> = Single
        .create { emitter ->
            val purchase = purchases?.first()
            if (purchase != null) {
                billingClient?.consumeAsync(purchase.purchaseToken) { responseCode, purchaseToken ->
                    if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                        emitter.onSuccess(purchase)
                    } else {
                        println("Can't allowMultiplePurchases, responseCode: $responseCode")
                        emitter.onError(BillingError(responseCode))
                    }
                }
            } else {
                emitter.onError(BillingError(-6699))
            }
        }

    override fun launchBuyWorkFlow(activity: Activity, skuDetails: SkuDetails): Completable = Completable
        .fromCallable {
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
            ?.forEach {
                billingClient?.consumeAsync(it.purchaseToken) { responseCode, purchaseToken ->
                    if (responseCode == BillingClient.BillingResponse.OK && purchaseToken != null) {
                        println("onPurchases Updated consumeAsync, purchases token dealed: $purchaseToken")
                    } else {
                        println("onPurchases some troubles happened: $responseCode")
                    }
                }
            }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        this.purchases = purchases
        purchasesUpdatesRelay.accept(responseCode)
    }
}