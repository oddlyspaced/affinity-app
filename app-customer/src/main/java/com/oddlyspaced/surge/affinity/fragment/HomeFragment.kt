package com.oddlyspaced.surge.affinity.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.oddlyspaced.surge.affinity.BuildConfig
import com.oddlyspaced.surge.affinity.util.Logger
import com.oddlyspaced.surge.affinity.R
import com.oddlyspaced.surge.affinity.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.affinity.service.GPSTrackerService
import com.oddlyspaced.surge.affinity.util.asGeoPoint
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    private val gpsTrackerService by lazy { GPSTrackerService(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        initOSMDroid()
        markCurrentLocation()
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
        binding.map.controller.setZoom(8.0)
    }

    private fun markCurrentLocation() {
        val marker = Marker(binding.map).apply {
            position = gpsTrackerService.fetchLocation().asGeoPoint()
            Logger.d("Current Location: ${gpsTrackerService.fetchLocation()}")
            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)?.apply { setTint(Color.RED) }
//            setInfoWindow(CustomInfoWindow(R.layout.layout_info_window_current_location, binding.map))
            showInfoWindow()
        }
        binding.map.overlays.add(marker)
    }
}