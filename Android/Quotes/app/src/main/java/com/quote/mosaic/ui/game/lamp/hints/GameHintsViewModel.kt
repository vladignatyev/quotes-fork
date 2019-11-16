package com.quote.mosaic.ui.game.lamp.hints

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class GameHintsViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {


    private val items = BehaviorProcessor.create<List<GameHintsModel>>()

    val state = State(
        items = items
    )

    override fun initialise() {
        items.onNext(
            listOf(
                GameHintsModel.Hint("product", 10),
                GameHintsModel.Hint("product", 10),
                GameHintsModel.Hint("product", 10),
                GameHintsModel.Hint("product", 10),
                GameHintsModel.Hint("product", 10),
                GameHintsModel.Hint("product", 10)
            )
        )
    }

    data class State(
        val inProgress: ObservableBoolean = ObservableBoolean(),
        val items: Flowable<List<GameHintsModel>>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameHintsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameHintsViewModel(schedulers, apiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}