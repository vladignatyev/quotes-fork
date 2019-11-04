package com.brain.words.puzzle.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDO(
    val id: Int,
    val nickname: String,
    val balance: Int,
    @JsonProperty("reward_per_doubleup")
    val rewardPerDoubleUp: Int

)