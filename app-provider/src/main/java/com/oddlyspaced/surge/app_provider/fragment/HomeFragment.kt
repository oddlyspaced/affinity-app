package com.oddlyspaced.surge.app_provider.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.oddlyspaced.surge.app_provider.App
import com.oddlyspaced.surge.app_provider.BuildConfig
import com.oddlyspaced.surge.app_provider.R
import com.oddlyspaced.surge.app_provider.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        initOSMDroid()
//        markCurrentLocation()
//        init()
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