package com.brain.words.puzzle.data.error

import com.brain.words.puzzle.data.model.ErrorDO
import okhttp3.ResponseBody

interface ResponseErrorMessageExtractor {
    fun errors(errorBody: ResponseBody?): List<ErrorDO>
}