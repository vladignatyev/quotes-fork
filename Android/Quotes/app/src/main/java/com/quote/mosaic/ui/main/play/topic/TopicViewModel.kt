package com.quote.mosaic.ui.main.play.topic

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.error.ResponseException
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.model.user.UserDO
import com.quote.mosaic.ui.main.play.topic.section.SectionModel
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import timber.log.Timber

class TopicViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager,
    private val mapper: TopicMapper,
    private val userPreferences: UserPreferences
) : AppViewModel() {

    private val sections = BehaviorProcessor.create<List<SectionModel>>()
    private val errorTrigger = ClearableBehaviorProcessor.create<Unit>()
    private val refreshTrigger = PublishProcessor.create<Unit>()

    val state = State(
        sections = sections,
        errorTrigger = errorTrigger.clearable(),
        refreshTrigger = refreshTrigger
    )

    fun setUp(model: TopicModel) {
        state.title.set(model.title)
        state.id.set(model.id)
    }

    override fun initialise() {
        val id = state.id.get() ?: 0
        sections.onNext(mapper.loadingState())
        refreshTrigger
            .flatMap {
                Flowable.zip(
                    apiClient.topic(id)
                        .map { mapper.toLocalModel(it) }.toFlowable().subscribeOn(schedulers.io()),
                    apiClient.profile()
                        .toFlowable().subscribeOn(schedulers.io()),
                    BiFunction { topics: List<SectionModel>, user: UserDO -> Pair(topics, user) }
                )
            }
            .subscribe({ (topics: List<SectionModel>, user: UserDO) ->
                userManager.setUser(user)
                if (topics.isEmpty()) {
                    sections.onNext(mapper.errorState())
                } else {
                    sections.onNext(topics)
                }
            }, {
                sections.onNext(mapper.errorState())
                Timber.e(it, "TopicViewModel init failed")
            }).untilCleared()

        refreshTrigger.onNext(Unit)

        userPreferences.state.colorChangedTrigger
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe {
                refreshTrigger.onNext(Unit)
            }.untilCleared()
    }

    fun refresh() {
        state.isRefreshing.set(true)
        val id = state.id.get() ?: 0

        apiClient
            .profile()
            .flatMap { user ->
                apiClient
                    .topic(id, true)
                    .subscribeOn(schedulers.io())
                    .map { Pair(mapper.toLocalModel(it), user) }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (topics: List<SectionModel>, user: UserDO) ->
                state.isRefreshing.set(false)
                userManager.setUser(user)
                if (topics.isEmpty()) {
                    sections.onNext(mapper.errorState())
                } else {
                    sections.onNext(topics)
                }
            }, {
                state.isRefreshing.set(false)
                sections.onNext(mapper.errorState())
                Timber.e(it, "TopicViewModel refresh() failed")
            }).untilCleared()
    }

    fun reset() {
        errorTrigger.clear()
    }

    fun openCategory(id: Int) {
        apiClient
            .openCategory(id)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                println("------------- Opened")
                refreshTrigger.onNext(Unit)
            }, {
                if (it is ResponseException.Application && it.error.lowBalance()) {
                    userManager.hasEmptyBalance()
                }
                Timber.e(it, "openCategory: $id failed")
            }).untilCleared()
    }

    data class State(
        val isRefreshing: ObservableBoolean = ObservableBoolean(false),
        val id: ObservableField<Int> = ObservableField(),
        val title: ObservableField<String> = ObservableField(),
        val sections: Flowable<List<SectionModel>>,
        val errorTrigger: Flowable<Unit>,
        val refreshTrigger: Flowable<Unit>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager,
        private val mapper: TopicMapper,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TopicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TopicViewModel(
                    schedulers,
                    apiClient,
                    userManager,
                    mapper,
                    userPreferences
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}