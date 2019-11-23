package com.quote.mosaic.data.model.overview

import com.fasterxml.jackson.annotation.JsonProperty

data class MainTopicDO(
    val id: Int,
    val title: String,
    @JsonProperty("bonus_reward")
    val bonusReward: Int,
    @JsonProperty("on_complete_achievement")
    val onCompleteAchievement: String?,
    val uri: String?
)