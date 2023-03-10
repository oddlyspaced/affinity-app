package com.oddlyspaced.surge.app.customer.fragment

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.freelapp.libs.locationfetcher.locationFetcher
import com.oddlyspaced.surge.app.common.*
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.Providers
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.app.customer.BuildConfig
import com.oddlyspaced.surge.app.customer.R
import com.oddlyspaced.surge.app.customer.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.app.customer.viewmodel.CustomerViewModel
import com.oddlyspaced.surge.app.customer.viewmodel.LocationType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val vm: CustomerViewModel by activityViewModels()

    private lateinit var currentLocation: Location
    private lateinit var userLocationMarker: Marker

    private val locationFetcher = locationFetcher("We need your permission to use your location for showing nearby items") {
        this.applyFrom(AffinityConfiguration.locationFetcherGlobalConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        initOSMDroid()
        CoroutineScope(Dispatchers.IO).launch {
            locationFetcher.location.collectLatest {
                it.fold({ error ->
                    Logger.d("Error: $error")
                }, { location ->
                    currentLocation = location
                    requireActivity().runOnUiThread {
                        markCurrentLocation(true)
                    }
                })
            }
        }
        pollCurrentLocation()
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

    private fun pollCurrentLocation() {
        Handler(Looper.getMainLooper()).postDelayed({
            CoroutineScope(Dispatchers.IO).launch {
                Logger.d("wow wow")
                locationFetcher.location.collectLatest {
                    it.fold({ error ->
                        Logger.d("Error: $error")
                    }, { location ->
                        Logger.d("Fetched location")
                        currentLocation = location
                        requireActivity().runOnUiThread {
                            markCurrentLocation()
                        }
                    })
                }
            }
            pollCurrentLocation()
        }, 60 * 1000)
    }

    private fun markCurrentLocation(shouldSetCenter: Boolean = false) {
        if (!isVisible)
            return
        if (!this@HomeFragment::currentLocation.isInitialized) {
            toast("Current location unavailable")
            return
        }
        val userPoint = currentLocation.asGeoPoint()
        if (this@HomeFragment::userLocationMarker.isInitialized) {
            requireActivity().runOnUiThread {
                binding.map.overlays.remove(userLocationMarker)
            }
        }
        userLocationMarker = Marker(binding.map).apply {
            position = userPoint
            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(Color.BLUE) }
            setInfoWindow(null)
        }
        requireActivity().runOnUiThread {
            if (shouldSetCenter) {
                binding.map.controller.setCenter(userPoint)
            }
            binding.map.overlays.add(userLocationMarker)
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
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProviderDetailsFragment(Providers(
                    arrayListOf(provider)
                ), null))
                return@setOnMarkerClickListener true
            }
        }
        binding.map.overlays.add(marker)
    }

    private fun init() {
        binding.fabMarkCurrent.setOnClickListener {
            markCurrentLocation(true)
        }
        binding.selectLocationPickup.apply {
            txSelectLocation.text = "Select pickup location"
            imgLocation.setColorFilter(Color.RED)
            root.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPickLocationFragment(LocationType.PICKUP))
            }
        }
        binding.selectLocationDrop.apply {
            txSelectLocation.text = "Select drop Location"
            imgLocation.setColorFilter(Color.GREEN)
            root.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPickLocationFragment(LocationType.DROP))
            }
        }

        binding.cardHomeSearch.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
        }

        vm.providers.observe(requireActivity()) { list ->
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

        vm.selectedLocation[LocationType.PICKUP]?.let { address ->
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

        vm.selectedLocation[LocationType.DROP]?.let { address ->
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