package com.quote.mosaic.ui.game

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.model.QuoteDO
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

class GameViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    private val quoteLoadedTrigger = BehaviorProcessor.create<String>()
    private val levelCompletedTrigger = ClearableBehaviorProcessor.create<Unit>()

    val state = State(
        quoteLoadedTrigger = quoteLoadedTrigger,
        levelCompletedTrigger = levelCompletedTrigger.clearable()
    )

    fun setUp(id: Int) {
        state.selectedCategory.set(id)
    }

    override fun initialise() {
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient
            .quotesList(selectedCategoryId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ quotes ->
                state.allQuotes.set(quotes)
                handleLevelChanges()
            }, {
                Timber.w(it, "GameViewModel init failed")
            }).untilCleared()
    }

    fun completeLevel() {
        val currentQuote = state.allQuotes.get().orEmpty().first { !it.complete }
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient
            .completeLevel(currentQuote.id)
            .andThen(apiClient.quotesList(selectedCategoryId))
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ quotes ->
                state.allQuotes.set(quotes)
                handleLevelChanges()
                levelCompletedTrigger.onNext(Unit)
            }, {
                Timber.w(it, "completeLevel failed")
            }).untilCleared()

    }

    private fun handleLevelChanges() {
        val quotes = state.allQuotes.get()!!
        state.totalLevel.set(quotes.count().toString())
        state.currentLevel.set(quotes.filter { it.complete }.count().toString())

        val currentQuote = quotes.first { !it.complete }
        quoteLoadedTrigger.onNext(currentQuote.splitted.joinToString(separator = " "))
    }

    fun reset() {
        levelCompletedTrigger.clear()
    }

    data class State(
        val currentLevel: ObservableField<String> = ObservableField(""),
        val totalLevel: ObservableField<String> = ObservableField(""),
        val selectedCategory: ObservableField<Int> = ObservableField(),
        val allQuotes: ObservableField<List<QuoteDO>> = ObservableField(),
        val quote: ObservableField<String> = ObservableField(),

        val quoteLoadedTrigger: Flowable<String>,
        val levelCompletedTrigger: Flowable<Unit>
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