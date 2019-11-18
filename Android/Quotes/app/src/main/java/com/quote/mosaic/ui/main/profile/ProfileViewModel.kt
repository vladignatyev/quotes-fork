package com.quote.mosaic.ui.main.profile

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.common.toFlowable
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.data.api.ApiClient
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

class ProfileViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager,
    private val userPreferences: UserPreferences
) : AppViewModel() {

    private val logoutTrigger = BehaviorProcessor.create<Unit>()

    val state = State(
        logoutTrigger = logoutTrigger
    )

    override fun initialise() {
        state.nameText.toFlowable()
            .startWith("")
            .map { it.isNotBlank() && it != userManager.getUserName() }
            .subscribe {
                state.saveEnabled.set(it)
            }.untilCleared()

        state.color.set(userPreferences.profileShapeResId())
        state.nameText.set(userManager.getUserName())
    }

    fun logout() {
        userManager.logout()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                logoutTrigger.onNext(Unit)
            }, {
                Timber.e(it, "ProfileViewModel logout failed")
            }).untilCleared()
    }

    fun changeColor(newColor: Int) {
        state.color.set(newColor)
        userPreferences.setBackgroundColor(newColor)
    }

    fun save() {

    }

    data class State(
        val logoutTrigger: Flowable<Unit>,
        val color: ObservableInt = ObservableInt(),
        val nameText: NonNullObservableField<String> = NonNullObservableField(""),
        val saveEnabled: ObservableBoolean = ObservableBoolean(),
        val loading: ObservableBoolean = ObservableBoolean()
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(schedulers, apiClient, userManager, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}