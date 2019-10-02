package com.brain.words.puzzle.quotes.ui.main.game.topic

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TopicModel(
    val id: String,
    val title: String
) : Parcelable