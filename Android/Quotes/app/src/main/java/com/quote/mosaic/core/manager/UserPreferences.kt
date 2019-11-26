package com.quote.mosaic.core.manager

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.PublishRelay
import com.quote.mosaic.R
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

class UserPreferences(
    private val sharedPreferences: SharedPreferences
) {

    private val colorChangedTrigger = PublishProcessor.create<Unit>()

    val state = State(
        colorChangedTrigger = colorChangedTrigger
    )

    data class State(
        val colorChangedTrigger: Flowable<Unit>
    )

    fun setBackgroundColor(colorRes: Int) {
        val color = when (colorRes) {
            R.drawable.ic_circle_shape_blue -> R.color.game_background_blue
            R.drawable.ic_circle_shape_gray -> R.color.game_background_gray
            R.drawable.ic_circle_shape_green -> R.color.game_background_green
            R.drawable.ic_circle_shape_purple -> R.color.game_background_purple
            R.drawable.ic_circle_shape_red -> R.color.game_background_red
            R.drawable.ic_circle_shape_black -> R.color.game_background_black
            else -> R.color.game_background_blue
        }

        sharedPreferences.edit().putInt(KEY_BACKGROUND_COLOR_RES_ID, color).apply()
        colorChangedTrigger.onNext(Unit)
    }

    fun getBackgroundColor(): Int =
        sharedPreferences.getInt(KEY_BACKGROUND_COLOR_RES_ID, R.color.bar_background_blue)

    fun getBackgroundBarColor(): Int = when (getBackgroundColor()) {
        R.color.game_background_blue -> R.color.bar_background_blue
        R.color.game_background_gray -> R.color.bar_background_gray
        R.color.game_background_green -> R.color.bar_background_green
        R.color.game_background_purple -> R.color.bar_background_purple
        R.color.game_background_red -> R.color.bar_background_red
        R.color.game_background_black -> R.color.bar_background_black
        else -> R.color.bar_background_blue
    }

    fun profileShapeResId() = when (getBackgroundColor()) {
        R.color.game_background_blue -> R.drawable.ic_circle_shape_blue
        R.color.game_background_gray -> R.drawable.ic_circle_shape_gray
        R.color.game_background_green -> R.drawable.ic_circle_shape_green
        R.color.game_background_purple -> R.drawable.ic_circle_shape_purple
        R.color.game_background_red -> R.drawable.ic_circle_shape_red
        R.color.game_background_black -> R.drawable.ic_circle_shape_black
        else -> R.drawable.ic_circle_shape_blue
    }

    fun getOverlayResId() = when (getBackgroundColor()) {
        R.color.game_background_blue -> R.drawable.background_category_open_overlay_blue
        R.color.game_background_gray -> R.drawable.background_category_open_overlay_gray
        R.color.game_background_green -> R.drawable.background_category_open_overlay_green
        R.color.game_background_purple -> R.drawable.background_category_open_overlay_purple
        R.color.game_background_red -> R.drawable.background_category_open_overlay_red
        R.color.game_background_black -> R.drawable.background_category_open_overlay_black
        else -> R.drawable.background_category_open_overlay_blue
    }

    companion object {
        private const val KEY_BACKGROUND_COLOR_RES_ID = "KEY_BACKGROUND_COLOR_RES_ID"
    }
}