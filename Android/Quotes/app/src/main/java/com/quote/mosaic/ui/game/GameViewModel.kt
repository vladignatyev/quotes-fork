package com.quote.mosaic.ui.game

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.model.overview.QuoteDO
import com.quote.mosaic.ui.game.hint.HintModel
import com.quote.mosaic.ui.game.hint.HintType
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import java.util.*

class GameViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager
) : AppViewModel() {

    private val onNextLevelReceived = BehaviorProcessor.create<List<String>>()
    private val onHintsReceived = BehaviorProcessor.create<List<HintModel>>()
    private val levelCompletedTrigger = ClearableBehaviorProcessor.create<Unit>()

    private val showBalanceTrigger = PublishProcessor.create<Unit>()
    private val showHintTriggered = PublishProcessor.create<String>()
    private val skipLevelTriggered = PublishProcessor.create<Unit>()

    val state = State(
        onNextLevelReceived = onNextLevelReceived,
        onHintsReceived = onHintsReceived,

        levelCompletedTrigger = levelCompletedTrigger.clearable(),
        showBalanceTrigger = showBalanceTrigger,
        showHintTriggered = showHintTriggered,
        skipLevelTriggered = skipLevelTriggered
    )

    fun setUp(id: Int) {
        state.selectedCategory.set(id)
    }

    override fun initialise() {
        loadLevel()

        apiClient.profile()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.userName.set(it.nickname)
                state.balance.set(it.balance.toString())
            }, {
                Timber.w(it, "User loading failed")
            }).untilCleared()
    }

    fun markLevelAsCompleted() {
        val currentQuote = state.allQuotes.get().orEmpty().first { !it.complete }
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient
            .completeLevel(currentQuote.id)
            .andThen(apiClient.quotesList(selectedCategoryId))
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                levelCompletedTrigger.onNext(Unit)
            }, {
                Timber.e(it, "completeLevel failed")
            }).untilCleared()
    }

    fun loadHints() {
        //TODO: change
        val hints = mutableListOf<HintModel>().apply {
            add(HintModel.Balance(state.balance.get() ?: "0"))
            add(HintModel.CoinHint(HintType.NEXT_WORD, "Узнать следующее слово", "5"))
            add(HintModel.SkipHint("Пропустить Уровень", "30"))

            state.currentQuote.get()?.author?.let {
                add(HintModel.CoinHint(HintType.AUTHOR, "Узнать автора цитаты", "1"))
            }

            add(HintModel.Close)
        }
        onHintsReceived.onNext(hints)
    }

    fun loadLevel() {
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient
            .quotesList(selectedCategoryId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ quotes ->
                state.allQuotes.set(quotes)
                state.totalLevel.set(quotes.count().toString())
                state.currentLevel.set(quotes.filter { it.complete }.count().plus(1).toString())

                val currentQuote = quotes.first { !it.complete }
                state.currentQuote.set(currentQuote)
                onNextLevelReceived.onNext(mixedQuote(currentQuote.splitted))
            }, {
                Timber.e(it, "GameViewModel init failed")
            }).untilCleared()
    }

    fun findNextWord() {
        val correctQuote = state.currentQuote.get()?.splitted!!
        val userVariantQuote = state.userVariantQuote.get().orEmpty()

        if (correctQuote.size == userVariantQuote.size) {
            var hint = ""
            correctQuote.forEachIndexed { index, word ->
                if (word != userVariantQuote[index]) {
                    showHintTriggered.onNext("$hint$word")
                    return
                } else {
                    hint += "$word "
                }
            }
        }
    }

    fun findAuthor() {
        state.currentQuote.get()?.author?.let {
            showHintTriggered.onNext(it)
        }
    }

    fun skipLevel() {
        skipLevelTriggered.onNext(Unit)
    }

    fun showBalance() {
        showBalanceTrigger.onNext(Unit)
    }

    fun setCurrentVariant(currentQuote: List<String>) {
        state.userVariantQuote.set(currentQuote)
        onNextLevelReceived.onNext(currentQuote)
    }

    private fun mixedQuote(quote: List<String>): List<String> =
        quote.shuffled(Random(quote.size.toLong()))

    data class State(
        val selectedCategory: ObservableField<Int> = ObservableField(),

        val currentLevel: NonNullObservableField<String> = NonNullObservableField(""),
        val totalLevel: NonNullObservableField<String> = NonNullObservableField(""),
        val balance: NonNullObservableField<String> = NonNullObservableField(""),
        val userName: NonNullObservableField<String> = NonNullObservableField(""),

        val allQuotes: ObservableField<List<QuoteDO>> = ObservableField(),
        val currentQuote: ObservableField<QuoteDO> = ObservableField(),
        val userVariantQuote: ObservableField<List<String>> = ObservableField(),

        val onNextLevelReceived: Flowable<List<String>>,
        val onHintsReceived: Flowable<List<HintModel>>,

        val levelCompletedTrigger: Flowable<Unit>,
        val showBalanceTrigger: Flowable<Unit>,
        val showHintTriggered: Flowable<String>,
        val skipLevelTriggered: Flowable<Unit>
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(schedulers, apiClient, userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}