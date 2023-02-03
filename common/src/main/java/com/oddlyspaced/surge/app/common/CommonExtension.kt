package com.oddlyspaced.surge.app.common

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.freelapp.libs.locationfetcher.LocationFetcher
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

object Logger {
    fun d(msg: String) {
        Log.d("Affinity", msg)
    }
}

fun Location?.asGeoPoint() = GeoPoint(this?.latitude ?: 0.0, this?.longitude ?: 0.0)
fun IGeoPoint?.asGeoPoint() = GeoPoint(this?.latitude ?: 0.0, this?.longitude ?: 0.0)
fun Context.toast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, length).show()
}
fun GeoPoint.asLocation() = com.oddlyspaced.surge.app.common.modal.Location(this.latitude, this.longitude)

fun LocationFetcher.Config.applyFrom(config: LocationFetcher.Config) {
    this.fastestInterval = config.fastestInterval
    this.interval = config.interval
    this.maxWaitTime = config.maxWaitTime
    this.priority = config.priority
    this.smallestDisplacement = config.smallestDisplacement
    this.isWaitForAccurateLocation = config.isWaitForAccurateLocation
    this.providers = config.providers
    this.numUpdates = config.numUpdates
    this.debug = config.debug
}