package com.brain.words.puzzle.quotes.ui.main.game.topic.category

import com.brain.words.puzzle.quotes.ui.main.game.topic.quote.QuoteModel

data class CategoryModel(
    val id: String,
    val title: String,
    val quotes: List<QuoteModel>
)