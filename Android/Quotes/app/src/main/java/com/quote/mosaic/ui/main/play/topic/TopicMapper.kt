package com.quote.mosaic.ui.main.play.topic

import android.content.Context
import com.quote.mosaic.R
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.data.model.CategoryDO
import com.quote.mosaic.data.model.TopicDO
import com.quote.mosaic.ui.main.play.topic.category.CategoryModel
import com.quote.mosaic.ui.main.play.topic.section.SectionModel
import kotlin.math.roundToInt


interface TopicMapper {
    fun loadingState(): List<SectionModel>
    fun toLocalModel(topicDO: TopicDO): List<SectionModel>
    fun errorState(): List<SectionModel>
}

class TopicMapperImpl(
    private val userPreferences: UserPreferences,
    private val context: Context
) : TopicMapper {

    override fun errorState(): List<SectionModel> = listOf(SectionModel.Error(false))

    override fun loadingState(): List<SectionModel> = listOf(
        SectionModel.Loading(
            listOf(
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading
            )
        ), SectionModel.Loading(
            listOf(
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading
            )
        ), SectionModel.Loading(
            listOf(
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading
            )
        ), SectionModel.Loading(
            listOf(
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading
            )
        ), SectionModel.Loading(
            listOf(
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading,
                CategoryModel.Loading
            )
        )
    )

    override fun toLocalModel(topicDO: TopicDO): List<SectionModel> = topicDO.sections
        .filter { section -> section.categories.any { it.totalLevels > 0 } }
        .map { section ->
            SectionModel.Item(
                id = section.id,
                title = section.title,
                categories = section.categories
                    .filter { it.totalLevels > 0 }
                    .sortedByDescending { it.isAvailableToUser }
                    .map { mapCategory(it) }
            )
        }

    private fun mapCategory(category: CategoryDO): CategoryModel = if (category.isAvailableToUser) {
        if (category.completedLevels == category.totalLevels) {
            //Completed
            CategoryModel.Completed(
                id = category.id,
                title = category.onCompleteAchievement ?: context.getString(R.string.topic_label_achievement_placeholder, category.title),
                iconUrl = category.image
            )
        } else {
            //User Can play
            CategoryModel.Open(
                id = category.id,
                title = category.title,
                completedQuotes = category.completedLevels * 100,
                totalQuotes = category.totalLevels * 100,
                percent = calculatePercent(category),
                iconUrl = category.image,
                overlayResId = userPreferences.getOverlayResId()
            )
        }
    } else {
        //Closed
        CategoryModel.Closed(
            id = category.id,
            title = category.title,
            price = category.priceToUnlock.toString(),
            iconUrl = category.image,
            loading = false
        )
    }

    private fun calculatePercent(
        category: CategoryDO
    ): Int = ((category.completedLevels.toDouble() / 100.0) * 100).roundToInt()
}
