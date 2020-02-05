package com.quote.mosaic.data.model.overview

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuoteDO(
    val id: Int,
    val text: String,
    val author: String?,
    val reward: Int,
    val beautiful: String,
    val complete: Boolean,
    val splitted: List<String>
): Parcelable