package com.oddlyspaced.surge.app.common

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.freelapp.libs.locationfetcher.LocationFetcher
import com.google.android.gms.auth.api.signin.internal.Storage
import com.oddlyspaced.surge.app.common.AffinityConfiguration.Companion.SHARED_PREFERENCE_NAME
import com.oddlyspaced.surge.app.common.modal.pref.DataType
import com.oddlyspaced.surge.app.common.modal.pref.StoragePreference
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

/**
 * tiny wrapper for logging statements
 */
object Logger {
    fun d(msg: String) {
        Log.d("Affinity", msg)
    }
}

/**
 * location wrappers for various use cases
 */
fun Location?.asGeoPoint() = GeoPoint(this?.latitude ?: 0.0, this?.longitude ?: 0.0)
fun IGeoPoint?.asGeoPoint() = GeoPoint(this?.latitude ?: 0.0, this?.longitude ?: 0.0)
fun Context.toast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, length).show()
}
fun GeoPoint.asLocation() = com.oddlyspaced.surge.app.common.modal.Location(this.latitude, this.longitude)

/**
 * location fetcher library config
 */
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

/**
 * util method that saves a preference
 * @param preference: the preference to update
 * @param value: the value to save
 */
fun Context.savePreference(preference: StoragePreference, value: Any) {
    this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().apply {
        when(preference.dataType) {
            DataType.BOOLEAN -> putBoolean(preference.name, value as Boolean)
            DataType.INT -> putInt(preference.name, value as Int)
            DataType.STRING -> putString(preference.name, value as String)
        }
        apply()
    }
}

/**
 * util method that reads a preference from storage
 */
fun Context.readPreference(preference: StoragePreference): Any {
    return when(preference.dataType) {
        DataType.BOOLEAN -> this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(preference.name, false)
        DataType.INT -> this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(preference.name, 0)
        DataType.STRING -> this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getString(preference.name, "") ?: ""
    }
}

/**
 * extensions for toast messages from any context
 */
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Fragment.toast(msg: String) = this.requireContext().toast(msg)