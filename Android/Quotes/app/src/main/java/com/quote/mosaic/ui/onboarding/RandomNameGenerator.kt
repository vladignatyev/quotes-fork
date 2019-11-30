package com.quote.mosaic.ui.onboarding

import android.content.Context
import com.quote.mosaic.R
import java.util.*

class RandomNameGenerator(
    private val context: Context
) {

    fun generateName(): String {
        val firstNameArr = context.resources.getStringArray(R.array.first_names)
        val secondNameArr = context.resources.getStringArray(R.array.second_names)

        return firstNameArr[Random().nextInt(firstNameArr.size)] + " " + secondNameArr[Random().nextInt(secondNameArr.size)]
    }


}