package com.quote.mosaic.ui.main.play.topic

import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.data.model.CategoryDO
import com.quote.mosaic.data.model.TopicDO
import com.quote.mosaic.ui.main.play.topic.category.CategoryModel
import com.quote.mosaic.ui.main.play.topic.section.SectionModel
import kotlin.math.roundToInt
import kotlin.random.Random


interface TopicMapper {
    fun loadingState(): List<SectionModel>
    fun toLocalModel(topicDO: TopicDO): List<SectionModel>
    fun errorState(): List<SectionModel>
}

class TopicMapperImpl(
    private val userPreferences: UserPreferences
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
                title = category.onCompleteAchievement ?: "Знаток " + category.title,
                iconUrl = getImageUrl(category)
            )
        } else {
            //User Can play
            CategoryModel.Open(
                id = category.id,
                title = category.title,
                completedQuotes = 30,
                totalQuotes = category.totalLevels * 100,
                percent = Random(category.id).nextInt(0, 100),
                iconUrl = getImageUrl(category),
                overlayResId = userPreferences.getOverlayResId()
            )
        }
    } else {
        //Closed
        CategoryModel.Closed(
            id = category.id,
            title = category.title,
            price = category.priceToUnlock.toString(),
            iconUrl = getImageUrl(category)
        )
    }

    private fun getImageUrl(
        category: CategoryDO
    ): String = imageUrls[Random(category.id).nextInt(0, imageUrls.size)]

    private fun calculatePercent(
        category: CategoryDO
    ): Int = ((category.completedLevels.toDouble() / 100.0) * 100).roundToInt()

    private val imageUrls = listOf(
        "https://i.imgur.com/ROKBFS1.jpg",
        "https://i.imgur.com/4kG6S86.jpg",
        "https://i.imgur.com/KBRCocL.jpg",
        "https://i.imgur.com/Aw0azmF.jpg",
        "https://i.imgur.com/CSe7HVR.jpg",
        "https://i.imgur.com/f7dWEL1.jpg",
        "https://i.imgur.com/yCvT7Fn.jpg"
    )
}
