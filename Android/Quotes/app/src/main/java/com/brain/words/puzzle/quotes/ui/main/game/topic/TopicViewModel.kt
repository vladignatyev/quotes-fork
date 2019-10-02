package com.brain.words.puzzle.quotes.ui.main.game.topic

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.core.AppViewModel
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.core.rx.ClearableBehaviorProcessor
import com.brain.words.puzzle.quotes.ui.main.game.topic.quote.QuoteModel
import com.brain.words.puzzle.quotes.ui.main.game.topic.section.SectionModel
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class TopicViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    private val sections = BehaviorProcessor.create<List<SectionModel>>()
    private val errorTrigger = ClearableBehaviorProcessor.create<Unit>()

    val state = State(
        sections = sections,
        errorTrigger = errorTrigger.clearable()
    )

    fun setUp(model: TopicModel) {
        state.title.set(model.title)
        state.id.set(model.id)
    }

    override fun initialise() {
        sections.onNext(
            listOf(
                SectionModel(
                    "0", "Section 0", listOf(
                        QuoteModel("0", "Category 0", "", ""),
                        QuoteModel("0", "Category 1", "", ""),
                        QuoteModel("0", "Category 2", "", ""),
                        QuoteModel("0", "Category 3", "", ""),
                        QuoteModel("0", "Category 4", "", ""),
                        QuoteModel("0", "Category 5", "", ""),
                        QuoteModel("0", "Category 6", "", ""),
                        QuoteModel("0", "Category 7", "", "")
                    )
                ), SectionModel(
                    "1", "Section 1", listOf(
                        QuoteModel("0", "Category 0", "", ""),
                        QuoteModel("0", "Category 1", "", ""),
                        QuoteModel("0", "Category 2", "", ""),
                        QuoteModel("0", "Category 3", "", ""),
                        QuoteModel("0", "Category 4", "", ""),
                        QuoteModel("0", "Category 5", "", ""),
                        QuoteModel("0", "Category 6", "", ""),
                        QuoteModel("0", "Category 7", "", "")
                    )
                ), SectionModel(
                    "2", "Section 2", listOf(
                        QuoteModel("0", "Category 0", "", ""),
                        QuoteModel("0", "Category 1", "", ""),
                        QuoteModel("0", "Category 2", "", ""),
                        QuoteModel("0", "Category 3", "", ""),
                        QuoteModel("0", "Category 4", "", ""),
                        QuoteModel("0", "Category 5", "", ""),
                        QuoteModel("0", "Category 6", "", ""),
                        QuoteModel("0", "Category 7", "", "")
                    )
                ), SectionModel(
                    "3", "Section 3", listOf(
                        QuoteModel("0", "Category 0", "", ""),
                        QuoteModel("0", "Category 1", "", ""),
                        QuoteModel("0", "Category 2", "", ""),
                        QuoteModel("0", "Category 3", "", ""),
                        QuoteModel("0", "Category 4", "", ""),
                        QuoteModel("0", "Category 5", "", ""),
                        QuoteModel("0", "Category 6", "", ""),
                        QuoteModel("0", "Category 7", "", "")
                    )
                ), SectionModel(
                    "4", "Section 4", listOf(
                        QuoteModel("0", "Category 0", "", ""),
                        QuoteModel("0", "Category 1", "", ""),
                        QuoteModel("0", "Category 2", "", ""),
                        QuoteModel("0", "Category 3", "", ""),
                        QuoteModel("0", "Category 4", "", ""),
                        QuoteModel("0", "Category 5", "", ""),
                        QuoteModel("0", "Category 6", "", ""),
                        QuoteModel("0", "Category 7", "", "")
                    )
                )
            )
        )
    }

    fun refresh() {
        state.isRefreshing.set(true)
        val title = state.title.get()!!
        val id = state.id.get()!!

        state.isRefreshing.set(false)
    }

    fun reset() {
        errorTrigger.clear()
    }

    data class State(
        val isRefreshing: ObservableBoolean = ObservableBoolean(false),
        val id: ObservableField<String> = ObservableField(),
        val title: ObservableField<String> = ObservableField(),
        val sections: Flowable<List<SectionModel>>,
        val errorTrigger: Flowable<Unit>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TopicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TopicViewModel(
                    schedulers,
                    apiClient
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}