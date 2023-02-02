package com.oddlyspaced.surge.app_provider.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.freelapp.libs.locationfetcher.LocationFetcher
import com.freelapp.libs.locationfetcher.locationFetcher
import com.google.android.gms.location.LocationRequest
import com.oddlyspaced.surge.app_common.AffinityConfiguration
import com.oddlyspaced.surge.app_common.Logger
import com.oddlyspaced.surge.app_common.asGeoPoint
import com.oddlyspaced.surge.app_common.asLocation
import com.oddlyspaced.surge.app_common.modal.Address
import com.oddlyspaced.surge.app_common.modal.Location
import com.oddlyspaced.surge.app_provider.BuildConfig
import com.oddlyspaced.surge.app_provider.R
import com.oddlyspaced.surge.app_provider.databinding.FragmentEditBinding
import com.oddlyspaced.surge.app_provider.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EditFragment: Fragment(R.layout.fragment_edit) {

    private lateinit var binding: FragmentEditBinding
    private lateinit var userLocationMarker: Marker
    private var currentMarker: Marker? = null
    private var currentAddress: Address? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private val locationFetcher = locationFetcher("We need your permission to use your location for showing nearby items") {
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
        binding = FragmentEditBinding.bind(view)
        // edit -> active location of provider to serve
        // make changes to backend to accommodate these positional changes
        initOSMDroid()
        CoroutineScope(Dispatchers.IO).launch {
            markCurrentLocation()
        }
        init()
        setupTouchTargetOverlay()
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
        binding.map.controller.setZoom(AffinityConfiguration.DEFAULT_MAP_ZOOM)
    }

    private suspend fun markCurrentLocation() {
        locationFetcher.location.collectLatest {
            it.fold({ error ->
                Toast.makeText(requireContext(), "Error occurred while fetching location.", Toast.LENGTH_SHORT).show()
                Logger.d("ERROR: $error")
            }, { location ->
                val userPoint = location.asGeoPoint()
                if (this@EditFragment::userLocationMarker.isInitialized) {
                    requireActivity().runOnUiThread {
                        binding.map.overlays.remove(userLocationMarker)
                    }
                }
                userLocationMarker = Marker(binding.map).apply {
                    position = userPoint
                    setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app_common.R.drawable.ic_location)?.apply { setTint(
                        Color.BLUE) }
                    setInfoWindow(null)
                }
                requireActivity().runOnUiThread {
                    binding.map.controller.setCenter(userPoint)
                    binding.map.overlays.add(userLocationMarker)
                }
            })
        }

    }

    // handle touches on map
    private fun setupTouchTargetOverlay() {
        val overlay = object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = binding.map.projection
                val loc = projection.fromPixels(e.x.toInt(), e.y.toInt())
                setAddressForLocation(loc.asGeoPoint().asLocation())
                Logger.d("${loc.latitude} | ${loc.longitude}")
                binding.map.controller.setCenter(loc.asGeoPoint())
                addMarker(loc.asGeoPoint())
                return true
            }
        }
        binding.map.overlays.add(overlay)
    }

    private fun init() {
        binding.selectEditLocation.txSelectLocation.text = "Pick center of serving area"
        binding.selectEditLocation.imgLocation.setColorFilter(Color.BLACK)



    }

    private fun addMarker(point: GeoPoint) {
        if (currentMarker != null) {
            binding.map.overlays.remove(currentMarker)
        }
        currentMarker = Marker(binding.map).apply {
            position = point
            icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app_common.R.drawable.ic_location)?.apply { setTint(Color.RED) }
            setInfoWindow(null)
        }
        binding.map.overlays.add(currentMarker)
    }

    private fun setAddressForLocation(location: Location) {
        binding.selectEditLocation.txSelectLocation.text = "Loading..."
        homeViewModel.addressFromLocation(location, binding.map.zoomLevelDouble.toInt()).observe(requireActivity()) { result ->
            binding.selectEditLocation.txSelectLocation.text = result.displayName + "\n" + "lat: ${location.lat}, lon: ${location.lon}"
            currentAddress = Address(location, result.displayName ?: "${location.lat}, ${location.lon}")
        }
    }
}