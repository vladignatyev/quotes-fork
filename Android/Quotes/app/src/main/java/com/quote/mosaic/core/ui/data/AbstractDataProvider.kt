package com.quote.mosaic.core.ui.data

abstract class AbstractDataProvider {

    abstract val count: Int

    abstract class Data {
        abstract val id: Long

        abstract val viewType: Int

        abstract val text: String
    }

    abstract fun getItem(index: Int): Data

    abstract fun moveItem(fromPosition: Int, toPosition: Int)

    abstract fun getCurrentQuote(): List<String>

    abstract fun getFullQuote(): List<String>
}
