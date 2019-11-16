package com.quote.mosaic.ui.main.play.topic

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.error.ResponseException
import com.quote.mosaic.data.model.CategoryDO
import com.quote.mosaic.data.model.TopicDO
import com.quote.mosaic.data.model.UserDO
import com.quote.mosaic.R
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.ui.main.play.topic.category.CategoryModel
import com.quote.mosaic.ui.main.play.topic.section.SectionModel
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.random.Random

class TopicViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager
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
        refreshTrigger.flatMap {
            Flowable.zip(
                apiClient.topic(state.id.get() ?: 0)
                    .map { toLocalModel(it) }
                    .toFlowable()
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui()),

                apiClient.profile().toFlowable()
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui()),

                BiFunction { topics: List<SectionModel>, user: UserDO ->
                    Pair(topics, user)
                }
            )
        }.subscribe({ (topics: List<SectionModel>, user: UserDO) ->
            userManager.setUser(user)
            sections.onNext(topics)
        }, {
            Timber.w(it, "TopicViewModel init failed")
        }).untilCleared()

        refreshTrigger.onNext(Unit)
    }

    private fun toLocalModel(topicDO: TopicDO): List<SectionModel> = topicDO.sections
        .map { section ->
            SectionModel(
                id = section.id,
                title = section.title,
                categories = section.categories.filter { it.totalLevels > 0 }.map { category ->
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
                    percent = ((category.completedLevels.toDouble() / 100.0) * 100).roundToInt(),
                    backgroundId = getBackgroundId(category)
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

    private fun getBackgroundId(category: CategoryDO): Int {
        val backgrounds = listOf(
            R.drawable.background_category_open_green,
            R.drawable.background_category_open_pink,
            R.drawable.background_category_open_purple,
            R.drawable.background_category_open_red,
            R.drawable.background_category_open_yellow
        )

        return backgrounds[Random.nextInt(0, backgrounds.size)]
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
                Timber.w(it, "openCategory: $id failed")
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
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TopicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TopicViewModel(
                    schedulers,
                    apiClient,
                    userManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}