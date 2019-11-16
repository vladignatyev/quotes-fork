package com.quote.mosaic.ui.game.lamp.buy

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.BillingManager
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class GameBuyViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val billingManager: BillingManager
) : AppViewModel() {

    private val items = BehaviorProcessor.create<List<GameBuyModel>>()

    val state = State(
        items = items
    )

    override fun initialise() {
        items.onNext(
            listOf(
                GameBuyModel.WatchVideo("watch video", "10"),
                GameBuyModel.PurchaseCoins("product", "10", 10, "10", "10", null),
                GameBuyModel.PurchaseCoins("product", "10", 10, "10", "10", null),
                GameBuyModel.PurchaseCoins("product", "10", 10, "10", "10", null),
                GameBuyModel.PurchaseCoins("product", "10", 10, "10", "10", null),
                GameBuyModel.PurchaseCoins("product", "10", 10, "10", "10", null),
                GameBuyModel.PurchaseCoins("product", "10", 10, "10", "10", null)
            )
        )
    }

    data class State(
        val inProgress: ObservableBoolean = ObservableBoolean(),
        val items: Flowable<List<GameBuyModel>>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val billingManager: BillingManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameBuyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameBuyViewModel(schedulers, apiClient, billingManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}