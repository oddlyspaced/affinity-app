package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationUpdateParameter(
    val id: Int,
    val newLocation: Location
): Parcelable