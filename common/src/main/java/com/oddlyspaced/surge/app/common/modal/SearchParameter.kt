package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * body class for post call to server for searching for providers
 * @param limitCount upper cap of providers count to fetch
 * @param limitDistance max distance of providers to search from
 * @param serviceFilters filter strings for searching
 * @param pickupLocation source location
 * @param dropLocation destination location
 */
@Parcelize
class SearchParameter(
    val limitCount: Int = 10,
    val limitDistance: Int,
    val serviceFilters: ArrayList<String>,
    val pickupLocation: Location,
    val dropLocation: Location,
): Parcelable