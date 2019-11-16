package com.quote.mosaic.data.error

import com.quote.mosaic.data.model.ErrorDO
import okhttp3.ResponseBody

interface ResponseErrorMessageExtractor {
    fun errors(errorBody: ResponseBody?): List<ErrorDO>
}