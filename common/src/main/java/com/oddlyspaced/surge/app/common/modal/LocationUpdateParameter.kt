package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * body class for post call to server to update the location of provider
 * @param id id of provider
 * @param newLocation location of provider to be updated
 */
@Parcelize
data class LocationUpdateParameter(
    val id: Int,
    val newLocation: Location
): Parcelable