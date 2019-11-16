package com.quote.mosaic.ui.main.play.topic.category

sealed class CategoryModel {

    data class Open(
        val id: Int,
        val title: String,
        val completedQuotes: Int,
        val totalQuotes: Int,
        val percent: Int,
        val iconUrl: String,
        val overlayResId: Int
    ) : CategoryModel()

    data class Completed(
        val id: Int,
        val title: String
    ) : CategoryModel()

    data class Closed(
        val id: Int,
        val title: String,
        val price: String,
        val iconUrl: String
    ) : CategoryModel()

    object Loading : CategoryModel()
}