package com.quote.mosaic.ui.game

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.billing.BillingManagerResult
import com.quote.mosaic.core.billing.BillingProduct
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.model.overview.QuoteDO
import com.quote.mosaic.data.model.user.UserDO
import com.quote.mosaic.ui.game.hint.HintModel
import com.quote.mosaic.ui.game.hint.HintType
import com.quote.mosaic.ui.main.play.topup.TopUpProductModel
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import kotlin.random.Random

class GameViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager,
    private val billingManager: BillingManager
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

    //============ Game =============//
    fun loadLevel() {
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient.profile()
            .flatMap { user ->
                apiClient.quotesList(selectedCategoryId)
                    .subscribeOn(schedulers.io())
                    .map { Pair(it, user) }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (quotes: List<QuoteDO>, user: UserDO) ->
                state.allQuotes.set(quotes)
                state.totalLevel.set(quotes.count().toString())
                state.currentLevel.set(quotes.filter { it.complete }.count().plus(1).toString())

                val currentQuote = quotes.first { !it.complete }
                state.currentQuote.set(currentQuote)
                onNextLevelReceived.onNext(currentQuote.splitted.shuffled(Random(currentQuote.id)))

                userManager.setUser(user)
                state.userName.set(user.nickname)
                state.balance.set(user.balance.toString())
            }, {
                Timber.e(it, "GameViewModel init failed")
            }).untilCleared()
    }

    fun setCurrentVariant(currentQuote: List<String>) {
        state.userVariantQuote.set(currentQuote)
        onNextLevelReceived.onNext(currentQuote)
    }

    fun markLevelAsCompleted() {
        val currentQuote = state.allQuotes.get().orEmpty().first { !it.complete }
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient
            .completeLevel(currentQuote.id)
            .andThen(apiClient.quotesList(selectedCategoryId))
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ quotes ->
                state.isLastQuote.set(quotes.none { !it.complete })
                prepareSuccessDialog()
                levelCompletedTrigger.onNext(Unit)
            }, {
                Timber.e(it, "completeLevel failed")
            }).untilCleared()
    }

    //============ Hint Dialog =============//
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

    //============ Success Dialog =============//
    private fun prepareSuccessDialog() {
        val currentQuote = state.currentQuote.get()!!

        state.successQuote.set(currentQuote.beautiful)
        state.successAuthor.set(currentQuote.author.orEmpty())
        state.successWinningCoins.set(currentQuote.reward.toString())
        state.successWinningDoubledCoins.set((currentQuote.reward * 2).toString())

//        TODO: find a way
//        state.successWinningCategoryCoins.set("30")
//        state.successWinningAchievement.set("Знаток всего подряд")
    }

    fun showDoubleUpVideo(activity: Activity) {
        val billingProduct: BillingProduct.TestSku = billingManager
            .availableSkus()
            .filterIsInstance<BillingProduct.TestSku>()
            .first()

        val product = TopUpProductModel.Free(
            id = billingProduct.remoteBro.id,
            title = billingProduct.remoteBro.title,
            iconUrl = billingProduct.remoteBro.imageUrl,
            billingProduct = billingProduct.skuDetails
        )

        billingManager.launchBuyWorkFlow(activity, product)
            .onErrorComplete()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe()
            .untilCleared()

        billingManager
            .billingResultTrigger()
            .flatMap { result ->
                apiClient
                    .profile()
                    .map { Pair(result, it) }
                    .subscribeOn(schedulers.io())
                    .toFlowable()
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (result: BillingManagerResult, user: UserDO) ->
                when (result) {
                    is BillingManagerResult.Success -> {
                        userManager.setUser(user)
                        state.balance.set(user.balance.toString())
                        billingManager.warmUp()
                    }
                }
            }, {
                Timber.e(it, "billingResultTrigger failed")
            }).untilCleared()
    }

    fun doubleUpPossible(): Boolean =
        billingManager.availableSkus()
            .filterIsInstance<BillingProduct.TestSku>()
            .isNotEmpty()

    fun reset() {
        levelCompletedTrigger.clear()
    }

    data class State(
        //============ Main/UI ==============//
        val selectedCategory: ObservableField<Int> = ObservableField(),
        val currentLevel: NonNullObservableField<String> = NonNullObservableField(""),
        val totalLevel: NonNullObservableField<String> = NonNullObservableField(""),
        val balance: NonNullObservableField<String> = NonNullObservableField(""),
        val userName: NonNullObservableField<String> = NonNullObservableField(""),

        //============ Game ===============//
        val allQuotes: ObservableField<List<QuoteDO>> = ObservableField(),

        val isLastQuote: ObservableBoolean = ObservableBoolean(),
        val currentQuote: ObservableField<QuoteDO> = ObservableField(),
        val userVariantQuote: ObservableField<List<String>> = ObservableField(),

        val onNextLevelReceived: Flowable<List<String>>,
        val levelCompletedTrigger: Flowable<Unit>,

        //============ Hint Dialog ===============//
        val onHintsReceived: Flowable<List<HintModel>>,
        val showBalanceTrigger: Flowable<Unit>,
        val showHintTriggered: Flowable<String>,
        val skipLevelTriggered: Flowable<Unit>,

        //============ Success Dialog =============//
        val successQuote: NonNullObservableField<String> = NonNullObservableField(""),
        val successAuthor: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningCoins: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningDoubledCoins: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningCategoryCoins: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningAchievement: NonNullObservableField<String> = NonNullObservableField("")
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager,
        private val billingManager: BillingManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(schedulers, apiClient, userManager, billingManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}