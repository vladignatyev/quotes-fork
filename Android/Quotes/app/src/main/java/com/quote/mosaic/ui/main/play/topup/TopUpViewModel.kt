package com.quote.mosaic.ui.main.play.topup

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.billing.BillingManagerResult
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.model.purchase.RemoteProductTag
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
        loadProducts()

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
                        billingManager.warmUp()
                        successTrigger.onNext(Unit)
                    }
                    is BillingManagerResult.Retry -> {
                        state.loading.set(false)
                        userManager.setUser(user)
                        state.balance.set(user.balance.toString())
                        Timber.e(
                            result.cause, "Billing Failed for user: ${userManager.getSession()}"
                        )
                        failureTrigger.onNext(Unit)
                    }
                }
            }, {
                Timber.e(it, "billingResultTrigger failed")
            }).untilCleared()
    }

    fun loadProducts() {
        products.onNext(
            productsMapper.toLocaleModel(billingManager.availableSkus().filter {
                it.remoteProduct.tags.contains(RemoteProductTag.TOP_UP)
            })
        )
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