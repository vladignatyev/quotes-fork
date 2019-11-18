package com.quote.mosaic.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CategoryDO(
    val id: Int,
    val icon: String,
    val title: String,
    @JsonProperty("section_id")
    val sectionId: Int,
    @JsonProperty("price_to_unlock")
    val priceToUnlock: Int,
    @JsonProperty("bonus_reward")
    val bonusReward: Int,
    @JsonProperty("on_complete_achievement")
    val onCompleteAchievement: String?,
    @JsonProperty("is_available_to_user")
    val isAvailableToUser: Boolean,
    @JsonProperty("progress_levels_total")
    val totalLevels: Int,
    @JsonProperty("progress_levels_complete")
    val completedLevels: Int,
    val image: String
)