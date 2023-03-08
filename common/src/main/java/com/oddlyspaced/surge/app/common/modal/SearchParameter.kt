package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SearchParameter(
    val limitCount: Int = 10,
    val limitDistance: Int,
    val serviceFilters: ArrayList<String>,
    val pickupLocation: Location,
    val dropLocation: Location,
): Parcelable