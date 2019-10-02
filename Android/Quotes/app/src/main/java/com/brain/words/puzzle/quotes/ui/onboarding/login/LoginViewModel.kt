package com.brain.words.puzzle.quotes.ui.onboarding.login

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.core.AppViewModel
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.core.common.toFlowable
import com.brain.words.puzzle.quotes.core.manager.FirebaseManager
import com.brain.words.puzzle.quotes.core.rx.NonNullObservableField
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit

class LoginViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val firebaseManager: FirebaseManager
) : AppViewModel() {

    private val loginSuccess = BehaviorProcessor.create<Unit>()
    private val loginFailure = PublishProcessor.create<String>()

    val state = State(
        loginFailure = loginFailure,
        loginSuccess = loginSuccess
    )

    override fun initialise() {
        state.nameText.toFlowable()
            .startWith("")
            .map { it.isNotBlank() }
            .subscribe {
                state.loginEnabled.set(it)
            }.untilCleared()

        firebaseManager.deviceId
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe {
                state.deviceId.set(it)
            }.untilCleared()
    }

    fun login() {
        val userName = state.nameText.get()

        state.loading.set(true)

        Completable.timer(1, TimeUnit.SECONDS, schedulers.ui())
            .subscribe {
                state.loading.set(false)
                loginSuccess.onNext(Unit)
            }.untilCleared()
    }

    data class State(
        val loading: ObservableBoolean = ObservableBoolean(),
        val loginEnabled: ObservableBoolean = ObservableBoolean(true),

        val deviceId: NonNullObservableField<String> = NonNullObservableField(""),

        val nameText: NonNullObservableField<String> = NonNullObservableField(""),

        val loginSuccess: Flowable<Unit>,
        val loginFailure: Flowable<String>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val firebaseManager: FirebaseManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(schedulers, apiClient, firebaseManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}