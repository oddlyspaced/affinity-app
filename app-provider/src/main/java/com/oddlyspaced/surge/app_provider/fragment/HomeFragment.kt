package com.oddlyspaced.surge.app_provider.fragment

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.fragment.app.Fragment
import arrow.core.left
import com.freelapp.libs.locationfetcher.LocationFetcher
import com.freelapp.libs.locationfetcher.locationFetcher
import com.google.android.gms.location.LocationRequest
import com.oddlyspaced.surge.app_provider.App
import com.oddlyspaced.surge.app_provider.BuildConfig
import com.oddlyspaced.surge.app_provider.R
import com.oddlyspaced.surge.app_provider.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.app_provider.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    val locationFetcher = locationFetcher("We need your permission to use your location for showing nearby items") {
        fastestInterval = 5.seconds
        interval = 15.seconds
        maxWaitTime = 2.minutes
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 50f
        isWaitForAccurateLocation = false
        providers = listOf(
            LocationFetcher.Provider.GPS,
            LocationFetcher.Provider.Network,
            LocationFetcher.Provider.Fused,
        )
        numUpdates = Int.MAX_VALUE
        debug = BuildConfig.DEBUG
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        initOSMDroid()
        CoroutineScope(Dispatchers.IO).launch {
            ok()
        }


//        markCurrentLocation()
//        init()
    }

    private suspend fun ok() {
        locationFetcher.location.collectLatest {
            it.fold({ error ->
                Logger.d("ERROR: $error")
            }, { location ->
                Logger.d("LOCATION: " + location.latitude + " " + location.longitude)
            })
        }
    }

    // handles runtime map configuration
    private fun initOSMDroid() {
        // setup map configuration
        Configuration.getInstance().let { config ->
            config.load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
            config.userAgentValue = BuildConfig.APPLICATION_ID
        }

        // setting up map
        binding.map.apply {
            setUseDataConnection(true)
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        }

        // zooming in by default
        binding.map.controller.setZoom(App.DEFAULT_MAP_ZOOM)
    }

//    private fun markCurrentLocation() {
//        val userPoint = gpsTrackerService.fetchLocation().asGeoPoint()
//        val marker = Marker(binding.map).apply {
//            position = userPoint
//            Logger.d("Current Location: ${gpsTrackerService.fetchLocation()}")
//            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
//            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)?.apply { setTint(Color.BLUE) }
//            setInfoWindow(null)
//        }
//        binding.map.controller.setCenter(userPoint)
//        binding.map.overlays.add(marker)
//    }

//    private fun init() {
//        binding.selectLocationPickup.apply {
//            txSelecLocation.text = "Select pickup location"
//            imgLocation.setColorFilter(Color.RED)
//        }
//        binding.selectLocationDrop.apply {
//            txSelecLocation.text = "Select Drop Location"
//            imgLocation.setColorFilter(Color.GREEN)
//        }
//
//        homeViewModel.providers.observe(requireActivity()) { list ->
//            list.forEach { provider ->
//                markProvider(provider)
//            }
//        }
//    }
}