package com.quote.mosaic.ui.main.play.game

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
import com.quote.mosaic.data.model.purchase.RemoteProductTag
import com.quote.mosaic.data.model.user.UserDO
import com.quote.mosaic.ui.main.play.topup.TopUpProductModel
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import kotlin.random.Random

class GameViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager,
    private val billingManager: BillingManager
) : AppViewModel() {

    private val onNextLevelReceived = PublishProcessor.create<List<String>>()
    private val levelCompletedTrigger = ClearableBehaviorProcessor.create<Unit>()

    private val hintReceivedTrigger = PublishProcessor.create<String>()
    private val skipLevelTriggered = PublishProcessor.create<Unit>()

    val state = State(
        onNextLevelReceived = onNextLevelReceived,

        levelCompletedTrigger = levelCompletedTrigger.clearable(),
        hintReceivedTrigger = hintReceivedTrigger,
        skipLevelTriggered = skipLevelTriggered
    )

    fun setUp(id: Int) {
        state.selectedCategory.set(id)
    }

    override fun initialise() {
        loadLevel()
        loadHints()

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
                val shuffled = currentQuote.splitted.shuffled(Random(currentQuote.id))
                state.userVariantQuote.set(shuffled)
                onNextLevelReceived.onNext(shuffled)

                userManager.setUser(user)
                state.userName.set(user.nickname)
                state.balance.set(user.balance.toString())
            }, {
                Timber.e(it, "GameViewModel init failed")
            }).untilCleared()
    }

    fun setCurrentVariant(userVariant: List<String>) {
        state.userVariantQuote.set(userVariant)
    }

    fun markLevelAsCompleted() {
        if (state.isLoading.get()) return

        state.isLoading.set(true)
        val currentQuote = state.allQuotes.get().orEmpty().first { !it.complete }
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient
            .completeLevel(currentQuote.id)
            .andThen(apiClient.quotesList(selectedCategoryId))
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ quotes ->
                state.isLoading.set(false)
                state.isLastQuote.set(quotes.none { !it.complete })
                prepareSuccessDialog()
                levelCompletedTrigger.onNext(Unit)
            }, {
                state.isLoading.set(false)
                Timber.e(it, "completeLevel failed")
            }).untilCleared()
    }

    //============ Hints =============//
    private fun loadHints() {
        //TODO: change
    }

    fun findNextWord() {
        val correctQuote = state.currentQuote.get()?.splitted!!
        val userVariantQuote = state.userVariantQuote.get().orEmpty()

        var hint = ""
        correctQuote.forEachIndexed { index, word ->
            if (word != userVariantQuote[index]) {
                hintReceivedTrigger.onNext("$hint$word")
                return
            } else {
                hint += "$word "
            }
        }
    }

    fun findAuthor() {
        state.currentQuote.get()?.author?.let {
            hintReceivedTrigger.onNext(it)
        }
    }

    fun skipLevel() {
        skipLevelTriggered.onNext(Unit)
        markLevelAsCompleted()
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
        val billingProduct: BillingProduct = billingManager
            .availableSkus().first { it.remoteProduct.tags.contains(RemoteProductTag.DOUBLE_UP) }

        val product = TopUpProductModel.Free(
            id = billingProduct.remoteProduct.id,
            title = billingProduct.remoteProduct.title,
            iconUrl = billingProduct.remoteProduct.imageUrl,
            billingProduct = billingProduct.skuDetails,
            payload = state.selectedCategory.get().toString()
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

    fun doubleUpPossible(): Boolean = billingManager.availableSkus()
        .any { it.remoteProduct.tags.contains(RemoteProductTag.DOUBLE_UP) }

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
        val isLoading: ObservableBoolean = ObservableBoolean(),

        //============ Game ===============//
        val allQuotes: ObservableField<List<QuoteDO>> = ObservableField(),

        val isLastQuote: ObservableBoolean = ObservableBoolean(),
        val currentQuote: ObservableField<QuoteDO> = ObservableField(),
        val userVariantQuote: ObservableField<List<String>> = ObservableField(),

        val onNextLevelReceived: Flowable<List<String>>,
        val levelCompletedTrigger: Flowable<Unit>,

        //============ Hint Dialog ===============//
        val author: ObservableField<String> = ObservableField(),
        val hintReceivedTrigger: Flowable<String>,
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