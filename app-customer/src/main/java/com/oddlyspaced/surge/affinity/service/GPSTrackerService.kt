package com.oddlyspaced.surge.affinity.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.oddlyspaced.surge.affinity.util.Logger


class GPSTrackerService(private val context: Context) : Service(), LocationListener {

    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var canGetLocation = false

    val location: Location?
        get() = fetchLocation()

    companion object {
        const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10F
        const val MIN_TIME_BW_UPDATES = 1000 * 10L
    }

    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

    init {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        Logger.d("GPS Enabled: $isGPSEnabled")
        Logger.d("Network Enabled: $isNetworkEnabled")
        if (!isGPSEnabled && !isNetworkEnabled) {
            Logger.d("No Location Provider found!")
        }
        else {
            canGetLocation = true
            if (checkForPermission()) {
                fetchLocation()
            }
        }
    }

    private fun checkForPermission(): Boolean {
        val permsToCheck = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        var permRes = true
        for (perm in permsToCheck) {
            permRes = permRes && (ActivityCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED)
        }
        if (permRes) {
            // all permissions granted
            Logger.d("All permissions granted!")
        }
        else {
            // need to ask for permissions
            Logger.d("Permission Missing, please ask for Location permissions or grant them explicitly")
        }
        return permRes
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation(): Location? {
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        var bestAccuracy: Float = -1F
        // check all available providers to fetch reliable location
        for (provider in providers) {
            locationManager.getLastKnownLocation(provider).let { loc ->
                if (loc != null) {
                    if (bestLocation == null) {
                        bestLocation = loc
                        bestAccuracy = loc.accuracy
                    }
                    else if (loc.accuracy < bestAccuracy) { // TODO: Validate if the logic is other way around
                        bestLocation = loc
                        bestAccuracy = loc.accuracy
                    }
                }
            }
        }
        return bestLocation
    }

    fun stopUsingGPS() {
        locationManager.removeUpdates(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {
        // handle
    }
}
