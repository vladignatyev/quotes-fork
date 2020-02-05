package com.quote.mosaic.data.model.user

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDO(
    val id: Int,
    val nickname: String,
    val balance: Int,
    @JsonProperty("reward_per_doubleup")
    val rewardPerDoubleUp: Int,
    @JsonProperty("initial_profile_balance")
    val initialBalance: Int

)