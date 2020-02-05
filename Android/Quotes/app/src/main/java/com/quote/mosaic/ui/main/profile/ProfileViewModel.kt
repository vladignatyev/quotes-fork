package com.quote.mosaic.ui.main.profile

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.common.toFlowable
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.manager.UserManager
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

    private val successTrigger = ClearableBehaviorProcessor.create<Unit>()
    private val logoutTrigger = BehaviorProcessor.create<Unit>()

    val state = State(
        logoutTrigger = logoutTrigger,
        successTrigger = successTrigger.clearable()
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
                Timber.w(it, "ProfileViewModel logout failed")
            }).untilCleared()
    }

    fun changeColor(newColor: Int) {
        state.color.set(newColor)
        userPreferences.setBackgroundColor(newColor)
    }

    fun save() {
        val newName = state.nameText.get()
        state.loading.set(true)
        apiClient
            .changeUserName(newName)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.loading.set(false)
                userManager.saveUserName(it.nickname)
                userManager.setUser(it)
                successTrigger.onNext(Unit)
            }, {
                state.loading.set(false)
                Timber.w(it, "Failed to change username")
            }).untilCleared()
    }

    fun reset() {
        successTrigger.clear()
    }

    data class State(
        val logoutTrigger: Flowable<Unit>,
        val successTrigger: Flowable<Unit>,

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