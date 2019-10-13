package com.brain.words.puzzle.quotes.ui.main.game

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.core.AppViewModel
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.core.rx.ClearableBehaviorProcessor
import com.brain.words.puzzle.quotes.core.rx.NonNullObservableField
import com.brain.words.puzzle.quotes.ui.main.game.topic.TopicModel
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

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
        categories.onNext(
            listOf(
                TopicModel("", "Афоризмы"),
                TopicModel("", "Жизнь"),
                TopicModel("", "Музыка"),
                TopicModel("", "Кино")
            )
        )
    }

    data class State(
        val categories: Flowable<List<TopicModel>>,
        val errorTrigger: Flowable<Unit>,
        val loading: ObservableBoolean = ObservableBoolean(),
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