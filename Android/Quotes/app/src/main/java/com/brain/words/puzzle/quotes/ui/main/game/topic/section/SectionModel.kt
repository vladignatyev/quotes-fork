package com.brain.words.puzzle.quotes.ui.main.game.topic.section

import com.brain.words.puzzle.quotes.ui.main.game.topic.quote.QuoteModel

data class SectionModel(
    val id: String,
    val title: String,
    val quotes: List<QuoteModel>
)