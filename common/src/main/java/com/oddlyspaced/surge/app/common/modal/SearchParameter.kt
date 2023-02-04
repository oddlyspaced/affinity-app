package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SearchParameter(
    val limitCount: Int = 10,
    val limitDistance: Int = 10,
    val pickupLat: Double,
    val pickupLon: Double,
    val dropLat: Double,
    val dropLon: Double,
    val filterServices: ArrayList<String>
): Parcelable