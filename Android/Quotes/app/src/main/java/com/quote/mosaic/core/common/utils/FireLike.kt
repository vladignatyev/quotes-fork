package com.quote.mosaic.core.common.utils

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import lv.chi.firelike.IconEmitterConfig
import lv.chi.firelike.providers.*
import java.util.*

object FireLike {
    fun flame(iconResource: Int, random: Random = Random()) = IconEmitterConfig(
        iconResource = iconResource,
        interpolatorProvider = RandomInterpolatorProvider(
            AccelerateDecelerateInterpolator(),
            LinearInterpolator(),
            AccelerateInterpolator()
        ),
        durationProvider = RandomValueInRangeProvider(
            minValue = 600f,
            maxValue = 800f,
            random = random
        ),
        translationYProvider = RandomOffsetInRangeProvider(
            minOffset = -300f,
            maxOffset = -1000f,
            random = random
        ),
        translationXProvider = RandomCurveKeypointProvider(
            minOffset = 20f,
            maxOffset = 100f,
            maxKeyPoints = 10,
            random = random
        ),
        scaleValueProvider = RandomGrowShrinkProvider(
            minScale = 0.33f,
            random = random
        ),
        alphaValueProvider = FadeOutAlphaValueProvider()
    )
}