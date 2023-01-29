package com.oddlyspaced.surge.affinity.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.GeoPoint
import kotlin.math.*

/**
 * data class to hold a location representation
 */
@Parcelize
data class Location(
    val lat: Double,
    val lon: Double,
): Parcelable

fun Location.distanceTo(point: Location): Double {
    val dLat = deg2rad(abs(this.lat - point.lat))
    val dLon = deg2rad(abs(this.lon - point.lon))
    val a =
        sin(dLat / 2.0) * sin(dLat / 2) + cos(deg2rad(this.lat)) * cos(deg2rad(point.lat)) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return 6371 * c
}

fun deg2rad(deg: Double): Double {
    return deg * (Math.PI / 180.0)
}

fun Location.asGeoPoint() = GeoPoint(this.lat, this.lon)