package com.oddlyspaced.surge.affinity.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.oddlyspaced.surge.affinity.App
import com.oddlyspaced.surge.affinity.BuildConfig
import com.oddlyspaced.surge.affinity.R
import com.oddlyspaced.surge.affinity.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.app_common.modal.asGeoPoint
import com.oddlyspaced.surge.affinity.service.GPSTrackerService
import com.oddlyspaced.surge.affinity.viewmodel.HomeViewModel
import com.oddlyspaced.surge.app_common.AffinityConfiguration
import com.oddlyspaced.surge.app_common.Logger
import com.oddlyspaced.surge.app_common.asGeoPoint
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val gpsTrackerService by lazy { GPSTrackerService(requireContext()) }
    private val homeViewModel: HomeViewModel by activityViewModels()

    // stores state of home fragment
    private enum class HomeState {
        DEFAULT, PICKING_PICKUP, PICKING_DROP
    }
    private var currentState = HomeState.DEFAULT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        initOSMDroid()
        markCurrentLocation()
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
            Logger.d("Current Location: ${gpsTrackerService.fetchLocation()}")
            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app_common.R.drawable.ic_location)?.apply { setTint(Color.BLUE) }
            setInfoWindow(null)
        }
        binding.map.controller.setCenter(userPoint)
        binding.map.overlays.add(marker)
    }

    private fun markProvider(provider: com.oddlyspaced.surge.app_common.modal.Provider) {
        val marker = Marker(binding.map).apply {
            position = provider.location.asGeoPoint()
            icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app_common.R.drawable.ic_location)?.apply { setTint(Color.RED) }
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ ->
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProviderDetailsFragment(provider))
                return@setOnMarkerClickListener true
            }
        }
        binding.map.overlays.add(marker)
    }

    private fun init() {
        binding.selectLocationPickup.apply {
            txSelecLocation.text = "Select pickup location"
            imgLocation.setColorFilter(Color.RED)
            root.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPickLocationFragment())
            }
        }
        binding.selectLocationDrop.apply {
            txSelecLocation.text = "Select Drop Location"
            imgLocation.setColorFilter(Color.GREEN)
        }

        homeViewModel.providers.observe(requireActivity()) { list ->
            list.forEach { provider ->
                markProvider(provider)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d("Resumed Home Fragment")
        Logger.d("${homeViewModel.pickupLocation}")
    }
}