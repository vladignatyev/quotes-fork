package com.quote.mosaic.ui.main.play.topic.section

import com.quote.mosaic.ui.main.play.topic.category.CategoryModel

data class SectionModel(
    val id: Int,
    val title: String,
    val categories: List<CategoryModel>
)