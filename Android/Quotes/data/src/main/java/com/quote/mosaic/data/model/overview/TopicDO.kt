package com.quote.mosaic.data.model.overview

import com.fasterxml.jackson.annotation.JsonProperty
import com.quote.mosaic.data.model.overview.SectionDO

data class TopicDO(
    val id: Int,
    val title: String,
    @JsonProperty("bonus_reward")
    val bonusReward: Int,
    @JsonProperty("on_complete_achievement")
    val onCompleteAchievement: String?,
    val sections: List<SectionDO>
)