package com.quote.mosaic.ui.onboarding

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.BuildConfig
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.common.toFlowable
import com.quote.mosaic.core.ext.Digest
import com.quote.mosaic.core.manager.AdsManager
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.UserManager
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class OnboardingViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager,
    private val nameGenerator: RandomNameGenerator
) : AppViewModel() {

    private val loginSuccess = PublishProcessor.create<Unit>()
    private val loginFailure = PublishProcessor.create<Throwable>()

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

        generateRandomName()
    }

    fun login() {
        if (state.loading.get()) return
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
                loginFailure.onNext(it)
                Timber.w(it, "OnboardingViewModel login failed")
            }).untilCleared()
    }

    fun loadUser() {
        apiClient
            .profile()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.initialBalance.set(it.initialBalance.toString())
            }, {
                Timber.w(it, "loadUser failed")
            }).untilCleared()
    }

    fun generateRandomName() {
        state.nameText.set(nameGenerator.generateName())
    }

    data class State(
        val loading: ObservableBoolean = ObservableBoolean(),
        val loginEnabled: ObservableBoolean = ObservableBoolean(true),

        val deviceId: NonNullObservableField<String> = NonNullObservableField(""),

        val nameText: NonNullObservableField<String> = NonNullObservableField(""),
        val balance: NonNullObservableField<String> = NonNullObservableField("50"),
        val initialBalance: NonNullObservableField<String> = NonNullObservableField("100"),

        val loginSuccess: Flowable<Unit>,
        val loginFailure: Flowable<Throwable>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager,
        private val nameGenerator: RandomNameGenerator
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OnboardingViewModel(
                    schedulers,
                    apiClient,
                    userManager,
                    nameGenerator
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}