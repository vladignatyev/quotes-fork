package com.quote.mosaic.ui.main.top

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import timber.log.Timber

class TopViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    override fun initialise() {
        load()
    }

    fun load() {
        apiClient.globalTop()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({

            }, {
                Timber.w(it, "")
            }).untilCleared()
    }

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TopViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TopViewModel(schedulers, apiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}