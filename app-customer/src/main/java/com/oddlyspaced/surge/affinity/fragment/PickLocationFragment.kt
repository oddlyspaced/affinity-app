package com.oddlyspaced.surge.affinity.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.oddlyspaced.surge.affinity.BuildConfig
import com.oddlyspaced.surge.affinity.R
import com.oddlyspaced.surge.affinity.databinding.FragmentPickLocationBinding
import com.oddlyspaced.surge.affinity.service.GPSTrackerService
import com.oddlyspaced.surge.affinity.viewmodel.HomeViewModel
import com.oddlyspaced.surge.app_common.AffinityConfiguration
import com.oddlyspaced.surge.app_common.Logger
import com.oddlyspaced.surge.app_common.asGeoPoint
import com.oddlyspaced.surge.app_common.asLocation
import com.oddlyspaced.surge.app_common.modal.*
import com.oddlyspaced.surge.app_common.modal.asGeoPoint
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@AndroidEntryPoint
class PickLocationFragment : Fragment(R.layout.fragment_pick_location) {

    private lateinit var binding: FragmentPickLocationBinding
    private val gpsTrackerService by lazy { GPSTrackerService(requireContext()) }

    private val vm: HomeViewModel by activityViewModels()
    private var currentLocation: GeoPoint? = null
    private var currentMarker: Marker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPickLocationBinding.bind(view)

        initOSMDroid()
        markCurrentLocation()
        setupTouchTargetOverlay()
        init()
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

    private fun markCurrentLocation() {
        val userPoint = gpsTrackerService.fetchLocation().asGeoPoint()
        val marker = Marker(binding.map).apply {
            position = userPoint
            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app_common.R.drawable.ic_location)?.apply { setTint(
                Color.BLUE) }
            setInfoWindow(null)
        }
        binding.map.controller.setCenter(userPoint)
        binding.map.overlays.add(marker)
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

    // handle touches on map
    private fun setupTouchTargetOverlay() {
        val overlay = object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = binding.map.projection
                val loc = projection.fromPixels(e.x.toInt(), e.y.toInt())
                currentLocation = loc.asGeoPoint()
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
        binding.cardButtonSaveLocation.setOnClickListener {
            currentLocation?.let { loc ->
                vm.pickupLocation = Location(loc.latitude, loc.longitude)
            }
            findNavController().popBackStack()
        }
    }

    private fun setAddressForLocation(location: Location) {
        binding.txPickerAddress.text = "Loading..."
        vm.addressFromLocation(location, binding.map.zoomLevelDouble.toInt()).observe(requireActivity()) { result ->
            binding.txPickerAddress.text = result.displayName + "\n" + "lat: ${location.lat}, lon: ${location.lon}"
        }
    }

}