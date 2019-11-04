package com.brain.words.puzzle.data.api

import com.brain.words.puzzle.data.model.MainTopicDO
import com.brain.words.puzzle.data.model.TopicDO
import com.brain.words.puzzle.data.model.UserDO
import io.reactivex.Single

interface ApiClient {

    fun login(
        deviceId: String, timestamp: String, signature: String, nickname: String
    ): Single<String>

    fun profile(): Single<UserDO>

    fun topics(): Single<List<MainTopicDO>>

    fun topic(id: Int): Single<TopicDO>
}