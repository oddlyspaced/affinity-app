package com.oddlyspaced.surge.app.customer.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.freelapp.libs.locationfetcher.LocationFetcher
import com.freelapp.libs.locationfetcher.locationFetcher
import com.google.android.gms.location.LocationRequest
import com.oddlyspaced.surge.app.customer.service.GPSTrackerService
import com.oddlyspaced.surge.app.customer.viewmodel.HomeViewModel
import com.oddlyspaced.surge.app.customer.viewmodel.LocationType
import com.oddlyspaced.surge.app.common.AffinityConfiguration
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.asGeoPoint
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.app.customer.BuildConfig
import com.oddlyspaced.surge.app.customer.R
import com.oddlyspaced.surge.app.customer.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
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

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var userLocationMarker: Marker

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
        binding = FragmentHomeBinding.bind(view)

        initOSMDroid()
        CoroutineScope(Dispatchers.IO).launch {
            markCurrentLocation()
        }
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

    private suspend fun markCurrentLocation() {
        locationFetcher.location.collectLatest {
            it.fold({ error ->
                Toast.makeText(requireContext(), "Error occurred while fetching location.", Toast.LENGTH_SHORT).show()
                Logger.d("ERROR: $error")
            }, { location ->
                val userPoint = location.asGeoPoint()
                if (this@HomeFragment::userLocationMarker.isInitialized) {
                    requireActivity().runOnUiThread {
                        binding.map.overlays.remove(userLocationMarker)
                    }
                }
                userLocationMarker = Marker(binding.map).apply {
                    position = userPoint
                    setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(
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

    private fun markProvider(provider: Provider) {
        if (!isAdded)
            return
        val marker = Marker(binding.map).apply {
            position = provider.location.asGeoPoint()
            icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(Color.BLACK) }
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
            txSelectLocation.text = "Select pickup location"
            imgLocation.setColorFilter(Color.RED)
            root.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPickLocationFragment(LocationType.PICKUP))
            }
        }
        binding.selectLocationDrop.apply {
            txSelectLocation.text = "Select Drop Location"
            imgLocation.setColorFilter(Color.GREEN)
            root.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPickLocationFragment(LocationType.DROP))
            }
        }

        binding.cardHomeSearch.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
        }

        homeViewModel.providers.observe(requireActivity()) { list ->
            list.forEach { provider ->
                markProvider(provider)
            }
        }
    }

    private var pickupMarker: Marker? = null
    private var dropMarker: Marker? = null

    override fun onResume() {
        super.onResume()
        Logger.d("Resumed Home Fragment")

        homeViewModel.selectedLocation[LocationType.PICKUP]?.let { address ->
            binding.selectLocationPickup.txSelectLocation.text = address.address
            pickupMarker?.let { pickupMarker ->
                binding.map.overlays.remove(pickupMarker)
            }
            pickupMarker = Marker(binding.map).apply {
                position = address.location.asGeoPoint()
                icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(Color.RED) }
                setInfoWindow(null)
            }
            binding.map.overlays.add(pickupMarker)
        }

        homeViewModel.selectedLocation[LocationType.DROP]?.let { address ->
            binding.selectLocationDrop.txSelectLocation.text = address.address
            dropMarker?.let { dropMark ->
                binding.map.overlays.remove(dropMark)
            }
            dropMarker = Marker(binding.map).apply {
                position = address.location.asGeoPoint()
                icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(Color.GREEN) }
                setInfoWindow(null)
            }
            binding.map.overlays.add(dropMarker)
        }
    }
}