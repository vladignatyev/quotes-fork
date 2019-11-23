package com.quote.mosaic.ui.main.play.topup

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.SkuDetails
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.manager.billing.BillingManager
import com.quote.mosaic.core.manager.billing.BillingManagerResult
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.model.purchase.AvailableProductsDO
import com.quote.mosaic.data.model.user.UserDO
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

class TopUpViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val billingManager: BillingManager,
    private val userManager: UserManager,
    private val productsMapper: TopUpProductMapper
) : AppViewModel() {

    private val products = BehaviorProcessor.create<List<TopUpProductModel>>()
    private val failureTrigger = ClearableBehaviorProcessor.create<Unit>()
    private val successTrigger = ClearableBehaviorProcessor.create<Unit>()

    val state = State(
        products = products,
        failureTrigger = failureTrigger.clearable(),
        successTrigger = successTrigger.clearable()
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

        billingManager
            .billingResultTrigger()
            .flatMap { result ->
                apiClient
                    .profile()
                    .map { Pair(result, it) }
                    .subscribeOn(schedulers.io())
                    .toFlowable()
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (result: BillingManagerResult, user: UserDO) ->
                when (result) {
                    is BillingManagerResult.Loading -> {
                        state.loading.set(true)
                    }
                    is BillingManagerResult.Success -> {
                        state.loading.set(false)
                        userManager.setUser(user)
                        state.balance.set(user.balance.toString())
                        successTrigger.onNext(Unit)
                    }
                    is BillingManagerResult.Retry -> {
                        state.loading.set(false)
                        userManager.setUser(user)
                        state.balance.set(user.balance.toString())
                        Timber.e(
                            "Billing Failed for user: ${userManager.getSession()}", result.cause
                        )
                        failureTrigger.onNext(Unit)
                    }
                }
            }, {
                Timber.e("billingResultTrigger failed", it)
            }).untilCleared()
    }

    fun buyProduct(activity: Activity, model: TopUpProductModel) {
        billingManager
            .launchBuyWorkFlow(activity, model)
            .onErrorComplete()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe()
            .untilCleared()
    }

    fun reset() {
        successTrigger.clear()
        failureTrigger.clear()
    }

    data class State(
        val products: Flowable<List<TopUpProductModel>>,
        val failureTrigger: Flowable<Unit>,
        val successTrigger: Flowable<Unit>,

        val balance: ObservableField<String> = ObservableField(""),
        val name: ObservableField<String> = ObservableField(""),

        val loading: ObservableBoolean = ObservableBoolean()
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val billingManager: BillingManager,
        private val userManager: UserManager,
        private val productsMapper: TopUpProductMapper
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TopUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TopUpViewModel(
                    schedulers, apiClient, billingManager, userManager, productsMapper
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}