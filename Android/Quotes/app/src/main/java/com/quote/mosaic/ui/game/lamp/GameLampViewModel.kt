package com.quote.mosaic.ui.game.lamp

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import timber.log.Timber

class GameLampViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    val state = State()

    override fun initialise() {
        loadProfile()
    }

    private fun loadProfile() {
        apiClient.profile()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.coinsCount.set(it.balance.toString())
            }, {
                Timber.w(it, "GameLampViewModel loadProfile()")
            }).untilCleared()
    }

    data class State(
        val coinsCount: ObservableField<String> = ObservableField()
    )
    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameLampViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameLampViewModel(schedulers, apiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}