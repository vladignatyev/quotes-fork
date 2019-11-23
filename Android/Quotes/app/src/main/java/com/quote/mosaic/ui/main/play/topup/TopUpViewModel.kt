package com.quote.mosaic.ui.main.play.topup

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.billing.BillingManager
import com.quote.mosaic.data.model.purchase.AvailableProductsDO
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

class TopUpViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val billingManager: BillingManager,
    private val productsMapper: TopUpProductMapper
) : AppViewModel() {

    private val products = BehaviorProcessor.create<List<TopUpProductModel>>()

    val state = State(
        products = products
    )

    fun setUp(name: String?, balance: String?) {
        state.name.set(name)
        state.balance.set(balance)
    }

    override fun initialise() {
        state.loading.set(true)

        billingManager.start()
            .andThen(
                apiClient.getSkuList()
                    .subscribeOn(schedulers.io())
                    .flatMap { products ->
                        billingManager.getBuyVariants(products.rechargeable.map { remote -> remote.sku })
                            .map { Pair(products, it) }
                            .subscribeOn(schedulers.io())
                    })
            .map { (remoteProducts: AvailableProductsDO, billingProducts: List<SkuDetails>) ->
                productsMapper.toLocaleModel(remoteProducts, billingProducts)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.loading.set(false)
                products.onNext(it)
            }, {
                state.loading.set(false)
                Timber.e("Billing initialization failed", it)
            })
            .untilCleared()
    }

    fun buyProduct(activity: Activity, model: TopUpProductModel) {
        state.loading.set(true)
        billingManager
            .launchBuyWorkFlow(activity, model.billingProduct)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe()
            .untilCleared()
    }

    data class State(
        val products: Flowable<List<TopUpProductModel>>,

        val loading: ObservableBoolean = ObservableBoolean(),
        val balance: ObservableField<String> = ObservableField(""),
        val name: ObservableField<String> = ObservableField("")
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val billingManager: BillingManager,
        private val productsMapper: TopUpProductMapper
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TopUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TopUpViewModel(schedulers, apiClient, billingManager, productsMapper) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}