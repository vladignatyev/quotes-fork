package com.quote.mosaic.ui.main.play

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.model.user.UserDO
import com.quote.mosaic.ui.main.play.topic.TopicModel
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

class OverviewViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager
) : AppViewModel() {

    private val categories = BehaviorProcessor.create<List<TopicModel>>()

    val state = State(
        categories = categories
    )

    override fun initialise() {
        load()

        userManager
            .user()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe {
                state.balance.set(it.balance.toString())
                state.name.set(it.name)
            }.untilCleared()

        apiClient
            .subscribePushNotifications()
            .onErrorComplete()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe()
            .untilCleared()

    }

    fun load() {
        state.loading.set(true)
        apiClient.topics()
            .map { topics -> topics.map { TopicModel(it.id, it.title) } }
            .flatMap { topics -> apiClient.profile().map { Pair(topics, it) } }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (topics: List<TopicModel>, user: UserDO) ->
                state.loading.set(false)
                state.error.set(false)
                userManager.setUser(user)
                categories.onNext(topics)
            }, {
                state.loading.set(false)
                state.error.set(true)
                Timber.e(it, "OverviewViewModel init failed")
            }).untilCleared()
    }

    data class State(
        val categories: Flowable<List<TopicModel>>,

        val loading: ObservableBoolean = ObservableBoolean(),
        val error: ObservableBoolean = ObservableBoolean(),
        val balance: ObservableField<String> = ObservableField(""),
        val name: ObservableField<String> = ObservableField(""),

        val selectedPosition: NonNullObservableField<Int> = NonNullObservableField(0)
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OverviewViewModel(schedulers, apiClient, userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}