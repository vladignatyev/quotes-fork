package com.brain.words.puzzle.quotes.ui.main.play.topic.section

import com.brain.words.puzzle.quotes.ui.main.play.topic.category.CategoryModel

data class SectionModel(
    val id: Int,
    val title: String,
    val categories: List<CategoryModel>
)