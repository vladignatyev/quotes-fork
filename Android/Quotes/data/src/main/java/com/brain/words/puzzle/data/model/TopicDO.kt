package com.brain.words.puzzle.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TopicDO(
    val id: Int,
    val title: String,
    @JsonProperty("bonus_reward")
    val bonusReward: Int,
    @JsonProperty("on_complete_achievement")
    val onCompleteAchievement: String?,
    val sections: List<SectionDO>
)