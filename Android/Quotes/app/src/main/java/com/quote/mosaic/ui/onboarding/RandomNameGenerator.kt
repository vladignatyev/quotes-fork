package com.quote.mosaic.ui.onboarding

import android.content.Context
import com.quote.mosaic.R
import java.security.SecureRandom
import java.util.*

class RandomNameGenerator(
    private val context: Context
) {

    fun generateName(): String {
        val maleFirstNameArr = context.resources.getStringArray(R.array.first_names_males)
        val maleSecondNameArr = context.resources.getStringArray(R.array.second_names_males)

        val femaleFirstNameArr = context.resources.getStringArray(R.array.first_names_females)
        val femaleSecondNameArr = context.resources.getStringArray(R.array.second_names_females)


        val isMale = SecureRandom().nextBoolean()
        return if (isMale) {
            findName(maleFirstNameArr, maleSecondNameArr)
        } else {
            findName(femaleFirstNameArr, femaleSecondNameArr)
        }
    }


    private fun findName(firstNameArr: Array<String>, secondNameArr: Array<String>): String {

        val seed = UUID.randomUUID().toString().toByteArray()

        val secureRandom = SecureRandom().apply {
            setSeed(seed)
        }

        val firstName = firstNameArr[secureRandom.nextInt(firstNameArr.lastIndex)]
        val secondName = secondNameArr[secureRandom.nextInt(secondNameArr.lastIndex)]

        return "$firstName $secondName"
    }


}