package com.quote.mosaic.ui.game.hint

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.model.QuoteDO
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class HintViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    private val items = BehaviorProcessor.create<List<HintModel>>()
    private val hintTrigger = BehaviorProcessor.create<String>()
    private val skipTrigger = BehaviorProcessor.create<Unit>()

    fun setUp(remoteQuote: QuoteDO, variantQuote: ArrayList<String>) {
        state.correctQuote.set(remoteQuote.splitted)
        state.variantQuote.set(variantQuote.toList())
        state.author.set(remoteQuote.author)
    }

    val state = State(
        items = items,
        hintTrigger = hintTrigger,
        skipTrigger = skipTrigger
    )

    override fun initialise() {
        items.onNext(
            listOf(
                HintModel.Balance("100"),
                HintModel.CoinHint(HintType.NEXT_WORD, "Узнать следующее слово", "5"),
                HintModel.SkipHint("Пропустить Уровень", "30"),
                HintModel.CoinHint(HintType.AUTHOR, "Узнать автора цитаты", "1"),
                HintModel.Close
            )
        )
    }

    fun findNextWord() {
        val correctQuote = state.correctQuote.get().orEmpty()
        val variantQuote = state.variantQuote.get().orEmpty()

        var hint = ""
        correctQuote.forEachIndexed { index, word ->
            if (word != variantQuote[index]) {
                hintTrigger.onNext("$hint$word")
                return
            } else {
                hint += "$word "
            }
        }
    }

    fun findAuthor() {
        hintTrigger.onNext(state.author.get()!!)
    }

    fun skipLevel() {
        skipTrigger.onNext(Unit)
    }

    data class State(
        val items: Flowable<List<HintModel>>,
        val hintTrigger: Flowable<String>,
        val skipTrigger: Flowable<Unit>,

        val author: ObservableField<String> = ObservableField(""),
        val correctQuote: ObservableField<List<String>> = ObservableField(),
        val variantQuote: ObservableField<List<String>> = ObservableField()
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HintViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HintViewModel(schedulers, apiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}