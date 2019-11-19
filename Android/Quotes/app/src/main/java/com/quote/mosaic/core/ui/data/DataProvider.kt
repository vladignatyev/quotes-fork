package com.quote.mosaic.core.ui.data

import java.util.*

class ExampleDataProvider : AbstractDataProvider() {

    private lateinit var quote: String

    private val data: MutableList<ConcreteData> = LinkedList()

    override val count: Int get() = data.size

    fun addQuote(quote: String) {
        this.quote = quote
        val quoteArr = quote
            .replace("\u00A0", " ")
            .split(SEPARATOR_WHITESPACE)
            .shuffled()
            .toTypedArray()

        if (quote == quoteArr.joinToString(separator = SEPARATOR_WHITESPACE)) {
            quoteArr.toList().shuffled().toTypedArray()
        }
        fillInData(quoteArr)
    }

    override fun getItem(index: Int): Data {
        if (index < 0 || index >= count) {
            throw IndexOutOfBoundsException("index = $index")
        }

        return data[index]
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return

        val item = data.removeAt(fromPosition)
        data.add(toPosition, item)
    }

    override fun getFullQuote(): String {
        return quote.replace("\u00A0", " ")
    }

    override fun getCurrentQuote(): String {
        return data.joinToString(separator = SEPARATOR_WHITESPACE)
    }

    private fun fillInData(quoteArr: Array<String>) {
        for (j in quoteArr.indices) {
            val id = data.size.toLong()
            val viewType = 0
            val text = quoteArr[j]
            data.add(ConcreteData(id, viewType, text))
        }
    }

    class ConcreteData(
        override val id: Long,
        override val viewType: Int,
        override val text: String
    ) : Data() {

        override fun toString() = text
    }

    companion object {
        const val SEPARATOR_WHITESPACE = " "
    }
}
