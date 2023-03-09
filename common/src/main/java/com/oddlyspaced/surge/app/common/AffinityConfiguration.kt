package com.oddlyspaced.surge.app.common

import com.freelapp.libs.locationfetcher.LocationFetcher
import com.google.android.gms.location.LocationRequest
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * configuration class for various different params
 */
class AffinityConfiguration {
    companion object {
        private const val API_URL_LINK = "http://srivalab-compute.cse.iitk.ac.in"
        private const val API_URL_PORT = "5030"
        const val API_URL = "$API_URL_LINK:$API_URL_PORT"
        const val DEFAULT_MAP_ZOOM = 14
        val locationFetcherGlobalConfig = LocationFetcher.Config(
            rationale = "We need your permission to use your location for showing nearby items",
            fastestInterval = 5.seconds,
            interval = 15.seconds,
            maxWaitTime = 2.minutes,
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY,
            smallestDisplacement = 50f,
            isWaitForAccurateLocation = false,
            providers = listOf(
                LocationFetcher.Provider.GPS,
                LocationFetcher.Provider.Network,
                LocationFetcher.Provider.Fused
            ),
            numUpdates = Int.MAX_VALUE,
        )
        const val SHARED_PREFERENCE_NAME = "prefs"
    }
}