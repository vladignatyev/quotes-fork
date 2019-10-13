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
                    "0", "Вожди", listOf(
                        QuoteModel("0", "Сталин", "", ""),
                        QuoteModel("0", "Напалеон", "", ""),
                        QuoteModel("0", "Ленин", "", ""),
                        QuoteModel("0", "Мао Цзэдун", "", "")
                    )
                ), SectionModel(
                    "1", "Философы", listOf(
                        QuoteModel("0", "Конфуций", "", ""),
                        QuoteModel("0", "Будда", "", ""),
                        QuoteModel("0", "Ницше", "", ""),
                        QuoteModel("0", "Сократ", "", ""),
                        QuoteModel("0", "Платон", "", ""),
                        QuoteModel("0", "Аристотель", "", "")
                    )
                ), SectionModel(
                    "2", "Русские Классики", listOf(
                        QuoteModel("0", "Лермонтов", "", ""),
                        QuoteModel("0", "Достоевский", "", ""),
                        QuoteModel("0", "Гоголь", "", ""),
                        QuoteModel("0", "Пушкин", "", ""),
                        QuoteModel("0", "Чехов", "", ""),
                        QuoteModel("0", "Булгаков", "", ""),
                        QuoteModel("0", "Толстой", "", "")
                    )
                ), SectionModel(
                    "3", "Мировые Писатели", listOf(
                        QuoteModel("0", "Шекспир", "", ""),
                        QuoteModel("0", "Хемингуэй", "", ""),
                        QuoteModel("0", "Джек Лондон", "", ""),
                        QuoteModel("0", "Марк Твен", "", ""),
                        QuoteModel("0", "Джордж Оруэлл", "", ""),
                        QuoteModel("0", "Антуан де Сент-Экзюпери", "", "")
                    )
                ), SectionModel(
                    "4", "Политики", listOf(
                        QuoteModel("0", "Франклин Рузвельт", "", ""),
                        QuoteModel("0", "Маргарет Тетчер", "", ""),
                        QuoteModel("0", "Владимир Путин", "", ""),
                        QuoteModel("0", "Уинстон Черчилль", "", ""),
                        QuoteModel("0", "Авраам Линкольн", "", "")
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