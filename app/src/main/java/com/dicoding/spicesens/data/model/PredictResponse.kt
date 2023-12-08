package com.dicoding.spicesens.data.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PredictResponse (
    val id: Int,
    val confidence: String,
    var image: Bitmap
): Parcelable