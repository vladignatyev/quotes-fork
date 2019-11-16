package com.quote.mosaic.data.error

import com.quote.mosaic.data.model.ErrorDO
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject

object ApiErrorExtractor : ResponseErrorMessageExtractor {
    override fun errors(errorBody: ResponseBody?): List<ErrorDO> {
        val str = errorBody?.string()

        if (str == null || str.isBlank()) {
            return DEFAULT_ERROR
        }

        val errorBodyJson = try {
            JSONObject(str)
        } catch (e: JSONException) {
            null
        } ?: return DEFAULT_ERROR

        return if (errorBodyJson.has(PARAM_ERRORS) && !errorBodyJson.isNull(PARAM_ERRORS)) {

            val errorsObj = errorBodyJson.getJSONArray(PARAM_ERRORS)

            val errors = mutableListOf<ErrorDO>()

            for (x in 0 until errorsObj.length()) {
                val errObj = errorsObj[x] as JSONObject

                errors.add(
                    ErrorDO(
                        code = errObj.getInt(PARAM_ERROR_CODE),
                        field = errObj.getString(PARAM_ERROR_FIELD),
                        message = errObj.getString(PARAM_ERROR_MSG)
                    )
                )
            }

            return errors

        } else {
            DEFAULT_ERROR
        }
    }

    private const val PARAM_ERROR_CODE = "code"
    private const val PARAM_ERROR_MSG = "message"
    private const val PARAM_ERROR_FIELD = "field"
    private const val PARAM_ERRORS = "errors"
    private val DEFAULT_ERROR = listOf(ErrorDO(0, "root", ""))
}