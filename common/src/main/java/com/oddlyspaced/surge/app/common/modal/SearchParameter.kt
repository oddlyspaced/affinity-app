package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class SearchParameter(
    val limitCount: Int = 10,
    val limitDistance: Int = 10,
    val pickupPoint: Location,
    val dropPoint: Location,
    val filterServices: ArrayList<String>? = arrayListOf()
): Parcelable