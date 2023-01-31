package com.oddlyspaced.surge.affinity.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.oddlyspaced.surge.affinity.BuildConfig
import com.oddlyspaced.surge.affinity.R
import com.oddlyspaced.surge.affinity.databinding.FragmentPickLocationBinding
import com.oddlyspaced.surge.affinity.service.GPSTrackerService
import com.oddlyspaced.surge.app_common.AffinityConfiguration
import com.oddlyspaced.surge.app_common.asGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

class PickLocationFragment : Fragment(R.layout.fragment_pick_location) {

    private lateinit var binding: FragmentPickLocationBinding
    private val gpsTrackerService by lazy { GPSTrackerService(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPickLocationBinding.bind(view)

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

    private fun handleClick() {
//        binding.map.controller.
    }
}