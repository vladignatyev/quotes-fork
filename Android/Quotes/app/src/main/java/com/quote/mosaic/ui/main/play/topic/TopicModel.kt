package com.quote.mosaic.ui.main.play.topic

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TopicModel(
    val id: Int,
    val title: String
) : Parcelable