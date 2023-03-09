package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * body class for post call to server to update the area served by a provider
 * @param id id of provide
 * @param newArea new area served by the provider
 */

@Parcelize
data class AreaUpdateParameter(
    val id: Int,
    val newArea: AreaServed,
): Parcelable