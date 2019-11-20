package com.quote.mosaic.ui.game.success

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.model.QuoteDO

class GameSuccessViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    val state = State()

    fun setUp(quote: QuoteDO) {
        state.author.set(quote.author)
        state.quote.set(quote.beautiful)
        state.reward.set(quote.reward.toString())
    }

    override fun initialise() {

    }

    data class State(
        val quote: ObservableField<String> = ObservableField(),
        val reward: ObservableField<String> = ObservableField(),
        val author: ObservableField<String> = ObservableField()
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameSuccessViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameSuccessViewModel(schedulers, apiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}