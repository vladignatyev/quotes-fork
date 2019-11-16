package com.quote.mosaic.ui.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.data.UserManager
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

class ProfileViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager
) : AppViewModel() {

    private val logoutTrigger = BehaviorProcessor.create<Unit>()

    val state = State(
        logoutTrigger = logoutTrigger
    )

    override fun initialise() {

    }

    fun logout() {
        userManager.logout()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                logoutTrigger.onNext(Unit)
            }, {
                Timber.w(it, "ProfileViewModel logout failed")
            }).untilCleared()
    }

    data class State(
        val logoutTrigger: Flowable<Unit>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(schedulers, apiClient, userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}