package com.oddlyspaced.surge.app.common

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
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