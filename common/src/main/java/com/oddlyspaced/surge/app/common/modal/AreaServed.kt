package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * stores info regarding the area served by a provider
 * @param source source point location of the provider for the area they will serve
 * @param radius radius of the area they will serve around that point
 */
@Parcelize
data class AreaServed(
    val source: Location,
    val radius: Double,
): Parcelable

// checks if a provided location lies in the radius
fun AreaServed.isPointInRadius(point: Location): Boolean {
    return this.source.distanceTo(point) <= radius
}