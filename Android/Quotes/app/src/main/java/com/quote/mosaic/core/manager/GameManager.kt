package com.quote.mosaic.core.manager

import com.quote.mosaic.data.model.CategoryDO
import com.quote.mosaic.data.model.QuoteDO
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class GameManager() {

    private val category = BehaviorProcessor.create<CategoryDO>()
    private val quote = BehaviorProcessor.create<QuoteDO>()

    val state = State(
        category = category,
        quote = quote
    )

    fun setCurrentCategory(categoryDO: CategoryDO) {
        category.onNext(categoryDO)
    }

    fun setCurrentQuote(quoteDo: QuoteDO) {
        quote.onNext(quoteDo)
    }

    data class State(
        val category: Flowable<CategoryDO>,
        val quote: Flowable<QuoteDO>
    )
}