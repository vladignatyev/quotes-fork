package com.quote.mosaic.ui.main.play.game.utils

import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.quote.mosaic.R
import com.quote.mosaic.core.common.utils.findColor
import com.quote.mosaic.core.manager.UserPreferences

object SnackbarBuilder {

    fun showHintSnackbar(
        root: View,
        userPreferences: UserPreferences,
        hint: String
    ) {
        val snackbar = Snackbar.make(root, hint, Snackbar.LENGTH_INDEFINITE)
        snackbar.view.apply {
            findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5
            val green = root.context.findColor(R.color.bar_background_green)
            val purple = root.context.findColor(R.color.bar_background_purple)

            if (userPreferences.getBackgroundBarColor() != R.color.bar_background_green) {
                setBackgroundColor(green)
            } else {
                setBackgroundColor(purple)
            }
        }

        snackbar
            .setAction(R.string.shared_label_remember) { snackbar.dismiss() }
            .setActionTextColor(root.context.findColor(R.color.white))
            .show()
    }
}