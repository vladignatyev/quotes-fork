package com.brain.words.puzzle.quotes.ui.main.game.topic.category

sealed class CategoryModel {

    data class Open(
        val id: Int,
        val title: String,
        val completedQuotes: Int,
        val totalQuotes: Int,
        val percent: Int
    ) : CategoryModel()

    data class Completed(
        val id: Int,
        val title: String
    ) : CategoryModel()

    data class Closed(
        val id: Int,
        val title: String,
        val price: String
    ) : CategoryModel()
}