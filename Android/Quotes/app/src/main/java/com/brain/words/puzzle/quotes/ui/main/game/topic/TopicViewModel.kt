package com.brain.words.puzzle.quotes.ui.main.game.topic

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.data.model.CategoryDO
import com.brain.words.puzzle.data.model.TopicDO
import com.brain.words.puzzle.quotes.core.AppViewModel
import com.brain.words.puzzle.quotes.core.Schedulers
import com.brain.words.puzzle.quotes.core.rx.ClearableBehaviorProcessor
import com.brain.words.puzzle.quotes.ui.main.game.topic.category.CategoryModel
import com.brain.words.puzzle.quotes.ui.main.game.topic.section.SectionModel
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber
import kotlin.math.roundToInt

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
        val id = state.id.get() ?: 0
        apiClient
            .topic(id)
            .map { toLocalModel(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                sections.onNext(it)
            }, {
                Timber.w(it, "TopicViewModel init failed")
            }).untilCleared()
    }

    private fun toLocalModel(topicDO: TopicDO): List<SectionModel> = topicDO.sections
        .map { section ->
            SectionModel(
                id = section.id,
                title = section.title,
                categories = section.categories.map { category ->
                    //Opened
                    mapCategory(category)
                })
        }

    private fun mapCategory(category: CategoryDO): CategoryModel =
        if (category.isAvailableToUser) {
            if (category.completedLevels == category.totalLevels) {
                //Completed
                CategoryModel.Completed(
                    id = category.id,
                    title = category.onCompleteAchievement ?: "Знаток " + category.title
                )
            } else {
                //User Can play
                CategoryModel.Open(
                    id = category.id,
                    title = category.title,
                    completedQuotes = category.completedLevels,
                    totalQuotes = 100,
                    percent = ((category.completedLevels.toDouble() / 100.0) * 100).roundToInt()
                )
            }
        } else {
            //Closed
            CategoryModel.Closed(
                id = category.id,
                title = category.title,
                price = category.priceToUnlock.toString()
            )
        }

    fun refresh() {
        state.isRefreshing.set(true)
        val id = state.id.get() ?: 0
        apiClient
            .topic(id)
            .map { toLocalModel(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.isRefreshing.set(false)
                sections.onNext(it)
            }, {
                state.isRefreshing.set(false)
                Timber.w(it, "TopicViewModel refresh() failed")
            }).untilCleared()
    }

    fun reset() {
        errorTrigger.clear()
    }

    data class State(
        val isRefreshing: ObservableBoolean = ObservableBoolean(false),
        val id: ObservableField<Int> = ObservableField(),
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