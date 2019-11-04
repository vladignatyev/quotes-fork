package com.brain.words.puzzle.quotes.ui.main.game

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.data.model.MainTopicDO
import com.brain.words.puzzle.data.model.UserDO
import com.brain.words.puzzle.quotes.core.AppViewModel
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.core.rx.ClearableBehaviorProcessor
import com.brain.words.puzzle.quotes.core.rx.NonNullObservableField
import com.brain.words.puzzle.quotes.ui.main.game.topic.TopicModel
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

class GameViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    private val categories = BehaviorProcessor.create<List<TopicModel>>()
    private val errorTrigger = ClearableBehaviorProcessor.create<Unit>()

    val state = State(
        categories = categories,
        errorTrigger = errorTrigger.clearable()
    )

    override fun initialise() {
        Single.zip(
            apiClient.profile(),
            apiClient.topics().map { topics -> topics.map { TopicModel(it.id, it.title) }},
            BiFunction{ user: UserDO, topics: List<TopicModel> ->
                Pair(user, topics)
            })
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (user: UserDO, topics: List<TopicModel>) ->
                categories.onNext(topics)
                state.balance.set(user.balance.toString())
                state.name.set(user.nickname)
            }, {
                Timber.w(it, "GameViewModel init failed")
            }).untilCleared()
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
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(schedulers, apiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}