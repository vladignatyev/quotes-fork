package com.quote.mosaic.core.manager.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.model.purchase.PurchaseStatus
import com.quote.mosaic.ui.main.play.topup.TopUpProductModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface BillingManager {

    fun start(): Completable

    fun getBuyVariants(skuList: List<String>): Single<List<SkuDetails>>

    fun launchBuyWorkFlow(activity: Activity, product: TopUpProductModel): Completable

    fun billingResultTrigger(): Flowable<BillingManagerResult>

}

class BillingManagerImpl(
    private val context: Context,
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : PurchasesUpdatedListener, BillingManager {

    private val disposableBag = CompositeDisposable()

    private var billingClient: BillingClient? = null

    private var pendingProduct: TopUpProductModel? = null

    private val resultTrigger = PublishProcessor.create<BillingManagerResult>()

    init {
        clearCachedHistory()
    }

    override fun billingResultTrigger(): Flowable<BillingManagerResult> = resultTrigger

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

    override fun launchBuyWorkFlow(activity: Activity, product: TopUpProductModel): Completable =
        Completable.fromCallable {
            pendingProduct = product
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(product.billingProduct)
                .build()
            billingClient?.launchBillingFlow(activity, billingFlowParams)
        }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?, purchases: MutableList<Purchase>?
    ) {
        when (billingResult?.responseCode) {
            BillingClient.BillingResponseCode.OK -> consumePurchase(purchases)
            else -> {
                Timber.e("onPurchasesUpdated() failed with code: ${billingResult?.responseCode}")
                clearCachedHistory()
                resultTrigger.onNext(BillingManagerResult.Retry("clearCachedHistory() called"))
            }
        }
        purchases?.maxBy { it.purchaseTime }?.let {
            registerTokenOnServer(it)
        }
    }

    /**
     *  !!! Purchase consummation must have:
     *
     *  1. Allow multiple purchases for the same SKU
     *  2. To really approve purchase for the Google Play otherwise money will be refunded after Grace period - 72h
     *
     * */
    private fun consumePurchase(purchases: MutableList<Purchase>?) {
        val purchaseToConsume = purchases?.maxBy { it.purchaseTime }

        if (purchaseToConsume != null) {
            val params = ConsumeParams
                .newBuilder()
                .setDeveloperPayload(purchaseToConsume.developerPayload)
                .setPurchaseToken(purchaseToConsume.purchaseToken)
                .build()
            billingClient?.consumeAsync(params) { result, purchaseToken ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Timber.e("Consume failed: ${result.responseCode}")
                }
            }
        } else {
            Timber.e("Consume failed purchases are empty, can't find purchase to consume")
        }
    }

    /**
     *
     * If code == ITEM_ALREADY_OWNED we need to clear history/cache to allow multiple purchases for the same product.
     *
     * */
    private fun clearCachedHistory() {
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

    /**
     *
     * After success purchase consummation we need:
     *
     * 1. To notify server about completed purchases
     * 2. Server will check purchase status
     * 3. In parallel we poll server to catch status update and when we get the result we need to update user entity
     *
     * */
    private fun registerTokenOnServer(
        purchase: Purchase
    ) {
        resultTrigger.onNext(BillingManagerResult.Loading)

        disposableBag += apiClient
            .registerPurchase(
                orderId = purchase.orderId,
                purchaseToken = purchase.purchaseToken,
                balanceRecharge = pendingProduct?.id.orEmpty()
            ).toFlowable()
            .flatMap { token ->
                apiClient
                    .getPurchaseStatus(token.purchaseId)
                    .repeatWhen { it.delay(1500, TimeUnit.MILLISECONDS) }
                    .take(60)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                when (it.status) {
                    PurchaseStatus.INVALID -> {
                        resultTrigger.onNext(BillingManagerResult.Retry("registerTokenOnServer failed: Invalid"))
                        disposableBag.clear()
                    }
                    PurchaseStatus.CANCELLED -> {
                        resultTrigger.onNext(BillingManagerResult.Retry("registerTokenOnServer failed: Canceled"))
                        disposableBag.clear()
                    }
                    PurchaseStatus.PURCHASED -> {
                        resultTrigger.onNext(BillingManagerResult.Success)
                        disposableBag.clear()
                    }
                    PurchaseStatus.UNKNOWN -> {
                        //Nothing, wait until status will changed
                    }
                }
            }, {
                resultTrigger.onNext(BillingManagerResult.Retry("registerTokenOnServer failed, try again"))
                disposableBag.clear()
            })
    }
}