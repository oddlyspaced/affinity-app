package com.oddlyspaced.surge.app_provider.util

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.oddlyspaced.surge.app_provider.App
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

object Logger {
    fun d(msg: String) {
        Log.d("CLIENT", msg)
    }
}

fun Location?.asGeoPoint() = GeoPoint(this?.latitude ?: 0.0, this?.longitude ?: 0.0)
fun IGeoPoint?.asGeoPoint() = GeoPoint(this?.latitude ?: 0.0, this?.longitude ?: 0.0)
fun AppCompatActivity.app() = this.application as App
fun Fragment.app() = this.requireActivity().application as App
fun Context.toast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, length).show()
}