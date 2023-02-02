package com.oddlyspaced.surge.affinity.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.freelapp.libs.locationfetcher.LocationFetcher
import com.freelapp.libs.locationfetcher.locationFetcher
import com.google.android.gms.location.LocationRequest
import com.oddlyspaced.surge.affinity.BuildConfig
import com.oddlyspaced.surge.affinity.R
import com.oddlyspaced.surge.affinity.databinding.FragmentSearchBinding
import com.oddlyspaced.surge.app_common.AffinityConfiguration
import com.oddlyspaced.surge.app_common.Logger
import com.oddlyspaced.surge.app_common.asGeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SearchFragment: Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding

    private val currentLocationFetcher = locationFetcher("We need your permission to use your location for showing nearby items") {
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSearchBinding.bind(view)

        init()
        initOSMDroid()
        CoroutineScope(Dispatchers.Default).launch {
            markCurrentLocation()
        }
    }

    private fun init() {
        binding.sliderSearchDistance.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.textSearchDistance.text = "${value.toInt()} km"
            }
        }
    }

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
        binding.map.controller.setZoom(AffinityConfiguration.DEFAULT_MAP_ZOOM)
    }

    private suspend fun markCurrentLocation() {
        currentLocationFetcher.location.collectLatest {
            it.fold({ error ->
                Toast.makeText(requireContext(), "Error occurred while fetching location", Toast.LENGTH_SHORT).show()
                Logger.d("Error while fetching location in SearchFragment: $error")
            }, { location ->
                val userPoint = location.asGeoPoint()
                val marker = Marker(binding.map).apply {
                    position = userPoint
                    setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app_common.R.drawable.ic_location)?.apply { setTint(Color.BLUE) }
                    setInfoWindow(null)
                }
                requireActivity().runOnUiThread {
                    binding.map.controller.setCenter(userPoint)
                    binding.map.overlays.add(marker)
                }
            })
        }
    }

}