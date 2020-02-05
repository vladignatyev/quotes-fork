package com.quote.mosaic.core.ext

import android.content.Context
import android.content.Intent
import com.quote.mosaic.R
import com.quote.mosaic.data.manager.UserManager

fun Intent.supportEmailIntent(
    context: Context,
    userManager: UserManager
): Intent = Intent().apply {
    type = "text/html"
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback.quotes.puzzle@gmail.com"))
    putExtra(Intent.EXTRA_TEXT, UserAgent.generateSupportText(context, userManager))
}

fun Intent.shareAppIntent(
    context: Context
) = Intent().apply {
    type = "text/plain"
    action = Intent.ACTION_SEND
    putExtra(
        Intent.EXTRA_TEXT,
        "${context.getString(R.string.profile_label_share_app)} ${context.getString(R.string.app_link)}"
    )
}