package com.brain.words.puzzle.quotes.ui.main.game.topic.section

import com.brain.words.puzzle.quotes.ui.main.game.topic.category.CategoryModel

data class SectionModel(
    val id: Int,
    val title: String,
    val categories: List<CategoryModel>
)