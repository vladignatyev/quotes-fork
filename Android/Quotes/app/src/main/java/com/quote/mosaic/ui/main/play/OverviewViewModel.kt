package com.quote.mosaic.ui.main.play

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.ui.main.play.topic.TopicModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber
import java.util.concurrent.TimeUnit

class OverviewViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager
) : AppViewModel() {

    private val categories = BehaviorProcessor.create<List<TopicModel>>()
    private val errorTrigger = ClearableBehaviorProcessor.create<Unit>()

    val state = State(
        categories = categories,
        errorTrigger = errorTrigger.clearable()
    )

    override fun initialise() {
        state.loading.set(true)
        apiClient.topics()
            .map { topics -> topics.map { TopicModel(it.id, it.title) } }
            .flatMap { list -> Single.timer(2, TimeUnit.SECONDS).flatMap { Single.just(list) } }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ topics: List<TopicModel> ->
                state.loading.set(false)
                categories.onNext(topics)
            }, {
                state.loading.set(false)
                Timber.w(it, "OverviewViewModel init failed")
            }).untilCleared()

        userManager
            .user()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe {
                state.balance.set(it.balance.toString())
                state.name.set(it.name)
            }.untilCleared()

    }

    data class State(
        val categories: Flowable<List<TopicModel>>,
        val errorTrigger: Flowable<Unit>,

        val loading: ObservableBoolean = ObservableBoolean(),
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