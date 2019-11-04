package com.brain.words.puzzle.quotes.ui.onboarding.login

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brain.words.puzzle.data.UserManager
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.BuildConfig
import com.brain.words.puzzle.quotes.core.AppViewModel
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.core.common.toFlowable
import com.brain.words.puzzle.quotes.core.ext.Digest
import com.brain.words.puzzle.quotes.core.rx.NonNullObservableField
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class LoginViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager
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
    }

    fun login() {
        state.loading.set(true)

        val userName = state.nameText.get()
        userManager.saveUserName(userName)

        val timestamp =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(Date())
        val deviceId = UUID.randomUUID().toString().replace("-", "")

        val maskedSecret = Digest.sha256(BuildConfig.SHARED_SECRET)
        val secret = "${maskedSecret}$deviceId|$timestamp"
        val signature = Digest.sha256(secret)

        apiClient
            .login(deviceId, timestamp, signature, userName)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.loading.set(false)
                userManager.saveSession(it)
                loginSuccess.onNext(Unit)
            }, {
                state.loading.set(false)
                loginFailure.onNext(it.message)
                Timber.w(it, "OnboardingViewModel login failed")
            }).untilCleared()
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
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(schedulers, apiClient, userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}