package com.quote.mosaic.ui.main.play.game

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.BuildConfig
import com.quote.mosaic.R
import com.quote.mosaic.core.AppViewModel
import com.quote.mosaic.core.Schedulers
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.billing.BillingManagerResult
import com.quote.mosaic.core.billing.BillingProduct
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.core.rx.ClearableBehaviorProcessor
import com.quote.mosaic.core.rx.NonNullObservableField
import com.quote.mosaic.data.api.ApiClient
import com.quote.mosaic.data.error.ResponseException
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.data.model.overview.QuoteDO
import com.quote.mosaic.data.model.purchase.RemoteProductTag
import com.quote.mosaic.data.model.user.UserDO
import com.quote.mosaic.ui.main.play.topup.TopUpProductModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GameViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient,
    private val userManager: UserManager,
    private val billingManager: BillingManager,
    private val analyticsManager: AnalyticsManager
) : AppViewModel() {

    private val onNextLevelReceived = BehaviorProcessor.create<List<String>>()
    private val levelCompletedTrigger = ClearableBehaviorProcessor.create<Unit>()

    private val insufficientBalanceTriggered = PublishProcessor.create<Unit>()
    private val hintReceivedTrigger = PublishProcessor.create<String>()
    private val skipLevelTriggered = PublishProcessor.create<Unit>()

    private var pendingHint: BillingProduct? = null

    val state = State(
        onNextLevelReceived = onNextLevelReceived,

        insufficientBalanceTriggered = insufficientBalanceTriggered,
        levelCompletedTrigger = levelCompletedTrigger.clearable(),
        hintReceivedTrigger = hintReceivedTrigger,
        skipLevelTriggered = skipLevelTriggered
    )

    fun setUp(id: Int) {
        state.selectedCategory.set(id)
    }

    override fun initialise() {
        state.isLoading.set(true)
        load()
    }

    fun load() {
        loadLevel()
        loadHints()

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
                        checkIfShowNextWordNeeded(result)
                        saveUser(user)
                        billingManager.warmUp()
                    }
                    is BillingManagerResult.Retry -> {
                        state.hintLoading.set(false)
                    }
                }
            }, {
                Timber.w(it, "billingResultTrigger failed")
            }).untilCleared()
    }

    private fun saveUser(user: UserDO) {
        userManager.setUser(user)
        state.userName.set(user.nickname)
        state.balance.set(user.balance.toString())
    }

    //============ Game =============//
    fun loadLevel() {
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient.profile()
            .flatMap { user ->
                apiClient.quotesList(selectedCategoryId)
                    .map { Pair(it, user) }
                    .subscribeOn(schedulers.io())
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (quotes: List<QuoteDO>, user: UserDO) ->
                state.error.set(false)
                state.isLoading.set(false)
                state.allQuotes.set(quotes)
                state.totalLevel.set(quotes.count().toString())
                state.currentLevel.set(quotes.filter { it.complete }.count().plus(1).toString())

                val currentQuote = quotes.firstOrNull { !it.complete }

                if (currentQuote == null) {
                    Timber.w("Category $selectedCategoryId is empty for user ${user.id}")
                    state.error.set(true)
                } else {
                    analyticsManager.logGameStarted(
                        currentQuote.id, state.selectedCategory.get() ?: 0
                    )
                    state.currentQuote.set(currentQuote)
                    state.author.set(currentQuote.author)
                    val shuffled = currentQuote.splitted.shuffled(Random(currentQuote.id))
                    state.userVariantQuote.set(shuffled)
                    onNextLevelReceived.onNext(shuffled)
                }

                saveUser(user)
            }, {
                state.isLoading.set(false)
                state.error.set(true)
                Timber.w(it, "GameViewModel init failed")
            }).untilCleared()
    }


    fun setCurrentVariant(userVariant: List<String>) {
        state.userVariantQuote.set(userVariant)
    }

    fun markLevelAsCompleted() {
        if (state.levelCompletedLoading.get()) return

        state.levelCompletedLoading.set(true)
        val currentQuote = state.allQuotes.get().orEmpty().first { !it.complete }
        val selectedCategoryId = state.selectedCategory.get() ?: 0

        apiClient
            .completeLevel(currentQuote.id)
            .andThen(apiClient.quotesList(selectedCategoryId))
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ quotes ->
                analyticsManager.logGameCompleted(currentQuote.id, selectedCategoryId)
                state.levelCompletedLoading.set(false)
                state.isLastQuote.set(quotes.none { !it.complete })
                prepareSuccessDialog()
                levelCompletedTrigger.onNext(Unit)
            }, {
                state.levelCompletedLoading.set(false)
                Timber.w(it, "completeLevel failed")
            }).untilCleared()
    }

    //============ Hints =============//
    private fun loadHints() {
        apiClient.getHints()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.hintAuthorId.set(it.author.first().id)
                state.hintAuthorCost.set(it.author.first().coinPrice.toString())

                state.hintNextWordId.set(it.nextWord.first().id)
                state.hintNextWordCost.set(it.nextWord.first().coinPrice.toString())

                state.hintSkipId.set(it.skipLevel.first().id)
                state.hintSkipCost.set(it.skipLevel.first().coinPrice.toString())
            }, {
                Timber.w(it, "getHints failed")
            }).untilCleared()
    }

    fun findAuthor() {
        val currentQuote = state.currentQuote.get() ?: return
        if (state.hintLoading.get() || currentQuote.author.isNullOrEmpty()) return
        state.hintLoading.set(true)
        apiClient
            .validateHint(state.hintAuthorId.get(), currentQuote.id.toString())
            .andThen(apiClient.profile().subscribeOn(schedulers.io()))
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.hintLoading.set(false)
                analyticsManager.logAuthorUsed(currentQuote.id)
                saveUser(it)
                hintReceivedTrigger.onNext(currentQuote.author)
            }, {
                state.hintLoading.set(false)
                Timber.w(it, "validateHint findAuthor() failed")
                when (it) {
                    is ResponseException.Application -> {
                        if (it.error.errorCode == 402) insufficientBalanceTriggered.onNext(Unit)
                    }
                }
            }).untilCleared()
    }

    fun skipLevel() {
        if (state.hintLoading.get()) return
        val currentQuote = state.currentQuote.get() ?: return
        val selectedCategoryId = state.selectedCategory.get() ?: return

        state.hintLoading.set(true)
        apiClient
            .validateHint(state.hintSkipId.get(), currentQuote.id.toString())
            .andThen(apiClient.quotesList(selectedCategoryId).subscribeOn(schedulers.io()))
            .flatMap { levelCompleted ->
                apiClient.profile().map { Pair(it, levelCompleted) }.subscribeOn(schedulers.io())
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ (user: UserDO, quotes: List<QuoteDO>) ->
                state.hintLoading.set(false)
                analyticsManager.logSkipUsed(currentQuote.id)
                saveUser(user)
                state.isLastQuote.set(quotes.none { !it.complete })
                prepareSuccessDialog()
                levelCompletedTrigger.onNext(Unit)
            }, {
                state.hintLoading.set(false)
                Timber.w(it, "validateHint skipLevel() failed")
                when (it) {
                    is ResponseException.Application -> {
                        if (it.error.errorCode == 402) insufficientBalanceTriggered.onNext(Unit)
                    }
                }
            }).untilCleared()
    }

    fun findNextWord() {
        if (state.hintLoading.get()) return
        findNextWord(false)
    }

    private fun findNextWord(isRewarded: Boolean) {
        val currentQuote = state.currentQuote.get() ?: return
        state.hintLoading.set(true)
        apiClient
            .validateHint(state.hintNextWordId.get(), currentQuote.id.toString())
            .andThen(apiClient.profile().subscribeOn(schedulers.io()))
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({
                state.hintLoading.set(false)
                analyticsManager.logNextWordUsed(currentQuote.id, isRewarded)
                saveUser(it)
                showNextWord()
            }, {
                state.hintLoading.set(false)
                Timber.w(it, "validateHint findAuthor() failed")
                when (it) {
                    is ResponseException.Application -> {
                        if (it.error.errorCode == 402) insufficientBalanceTriggered.onNext(Unit)
                    }
                }
            }).untilCleared()
    }

    fun findNextWordVideo(activity: Activity) {
        if (state.hintLoading.get()) return

        state.hintLoading.set(true)

        val billingProduct: BillingProduct = billingManager
            .availableSkus()
            .firstOrNull { it.remoteProduct.tags.contains(RemoteProductTag.HINT_NEXT_WORD) }
            ?: return

        pendingHint = billingProduct

        val product = TopUpProductModel.Free(
            id = billingProduct.remoteProduct.id,
            title = billingProduct.remoteProduct.title,
            iconUrl = billingProduct.remoteProduct.imageUrl,
            billingProduct = billingProduct.skuDetails
        )

        billingManager
            .launchBuyWorkFlow(activity, product)
            .onErrorComplete()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe()
            .untilCleared()

    }

    private fun showNextWord() {
        val correctQuote = state.currentQuote.get()?.splitted!!
        val userVariantQuote = state.userVariantQuote.get()

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

    private fun checkIfShowNextWordNeeded(result: BillingManagerResult.Success) {
        val hintSku =
            if (BuildConfig.DEBUG) pendingHint?.remoteProduct?.testSku else pendingHint?.remoteProduct?.sku

        if (result.sku == hintSku) {
            state.hintLoading.set(false)
            findNextWord(true)
        }
    }

    //============ Success Dialog =============//
    private fun prepareSuccessDialog() {
        val currentQuote = state.currentQuote.get()!!

        state.successQuote.set(currentQuote.beautiful)
        state.successAuthor.set(currentQuote.author.orEmpty())
        state.successWinningCoins.set(currentQuote.reward.toString())
        state.successWinningDoubledCoins.set((currentQuote.reward * 2).toString())
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
        analyticsManager.logDoubleUpClicked(
            state.currentQuote.get()?.id ?: 0, userManager.getUserBalance()
        )
        billingManager.launchBuyWorkFlow(activity, product)
            .onErrorComplete()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe()
            .untilCleared()
    }

    fun startDoubleUpDelay() {
        state.doubleUpLoading.set(true)
        Completable.timer(2500, TimeUnit.MILLISECONDS, schedulers.ui()).subscribe {
            state.doubleUpLoading.set(false)
            state.doubleUpColorRes.set(R.drawable.shape_solid_orange_rounded_corners)
        }.untilCleared()
    }

    fun doubleUpPossible(): Boolean = billingManager.availableSkus()
        .any { it.remoteProduct.tags.contains(RemoteProductTag.DOUBLE_UP) }

    fun verifyVideoProducts() {
        val hintVideoNextWordVisible = billingManager.availableSkus()
            .any { it.remoteProduct.tags.contains(RemoteProductTag.HINT_NEXT_WORD) }
        state.hintVideoNextWordVisible.set(hintVideoNextWordVisible)
    }

    fun reset() {
        levelCompletedTrigger.clear()
        state.doubleUpColorRes.set(R.drawable.shape_solid_gray_rounded_corners)
    }

    data class State(
        //============ Main/UI ==============//
        val selectedCategory: ObservableField<Int> = ObservableField(),
        val currentLevel: NonNullObservableField<String> = NonNullObservableField(""),
        val totalLevel: NonNullObservableField<String> = NonNullObservableField(""),
        val balance: NonNullObservableField<String> = NonNullObservableField(""),
        val userName: NonNullObservableField<String> = NonNullObservableField(""),
        val isLoading: ObservableBoolean = ObservableBoolean(),
        val error: ObservableBoolean = ObservableBoolean(),

        //============ Game ===============//
        val allQuotes: ObservableField<List<QuoteDO>> = ObservableField(),

        val isLastQuote: ObservableBoolean = ObservableBoolean(),
        val currentQuote: ObservableField<QuoteDO> = ObservableField(),
        val userVariantQuote: NonNullObservableField<List<String>> = NonNullObservableField(listOf("")),
        val levelCompletedLoading: ObservableBoolean = ObservableBoolean(),

        val onNextLevelReceived: Flowable<List<String>>,
        val levelCompletedTrigger: Flowable<Unit>,

        //============ Hint Dialog ===============//
        val author: ObservableField<String> = ObservableField(),
        val hintLoading: ObservableBoolean = ObservableBoolean(),
        val hintReceivedTrigger: Flowable<String>,
        val skipLevelTriggered: Flowable<Unit>,
        val insufficientBalanceTriggered: Flowable<Unit>,

        //How much hints
        val hintAuthorCost: NonNullObservableField<String> = NonNullObservableField(""),
        val hintSkipCost: NonNullObservableField<String> = NonNullObservableField(""),
        val hintNextWordCost: NonNullObservableField<String> = NonNullObservableField(""),

        val hintAuthorId: NonNullObservableField<String> = NonNullObservableField(""),
        val hintSkipId: NonNullObservableField<String> = NonNullObservableField(""),
        val hintNextWordId: NonNullObservableField<String> = NonNullObservableField(""),

        val hintVideoNextWordVisible: ObservableBoolean = ObservableBoolean(),

        //============ Success Dialog =============//
        val successQuote: NonNullObservableField<String> = NonNullObservableField(""),
        val successAuthor: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningCoins: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningDoubledCoins: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningCategoryCoins: NonNullObservableField<String> = NonNullObservableField(""),
        val successWinningAchievement: NonNullObservableField<String> = NonNullObservableField(""),
        val doubleUpColorRes: ObservableInt = ObservableInt(R.drawable.shape_solid_gray_rounded_corners),
        val doubleUpLoading: ObservableBoolean = ObservableBoolean()
    )

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient,
        private val userManager: UserManager,
        private val billingManager: BillingManager,
        private val analyticsManager: AnalyticsManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(
                    schedulers,
                    apiClient,
                    userManager,
                    billingManager,
                    analyticsManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}