package com.quote.mosaic.ui.main.play.topic.section

import com.quote.mosaic.ui.main.play.topic.category.CategoryModel

sealed class SectionModel {
    data class Item(
        val id: Int,
        val title: String,
        val categories: List<CategoryModel>
    ) : SectionModel()

    data class Loading(val categories: List<CategoryModel>) : SectionModel()

    data class Error(val loading: Boolean): SectionModel()
}