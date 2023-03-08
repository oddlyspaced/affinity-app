package com.oddlyspaced.surge.app.provider.fragment

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
import com.oddlyspaced.surge.app.common.modal.ProviderStatus
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.app.common.modal.flip
import com.oddlyspaced.surge.app.common.modal.pref.StoragePreference
import com.oddlyspaced.surge.app.provider.BuildConfig
import com.oddlyspaced.surge.app.provider.R
import com.oddlyspaced.surge.app.provider.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.app.provider.viewmodel.ProviderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var currentLocation: Location
    private lateinit var userLocationMarker: Marker
    private var currentMarker: Marker? = null

    private lateinit var binding: FragmentHomeBinding

    private val vm: ProviderViewModel by activityViewModels()
    private var status = ProviderStatus.UNDEFINED

    private val locationFetcher = locationFetcher("We need permission to fetch location") {
        this.applyFrom(AffinityConfiguration.locationFetcherGlobalConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        vm.providerId = requireContext().readPreference(StoragePreference.PREF_USER_ID) as Int

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

    override fun onResume() {
        super.onResume()
        vm.sourcePointAddress?.let { sourceAddress ->
            addMarker(sourceAddress.location.asGeoPoint())
            createCircleAroundPoint(sourceAddress.location.asGeoPoint(), vm.sourcePointWorkingRadius.toDouble())
            binding.textHomeStatus.text = if (status == ProviderStatus.ACTIVE) "Status: Active" else "Status: In Active"
            binding.textHomeStatus.setTextColor(if (status == ProviderStatus.ACTIVE) Color.GREEN else Color.RED)
            vm.updateProviderStatus(vm.providerId, status)
        } ?: run {
            vm.fetchProvider(vm.providerId).observe(requireActivity()) {
                addMarker(it.areaServed.source.asGeoPoint())
                createCircleAroundPoint(it.areaServed.source.asGeoPoint(), it.areaServed.radius)
                binding.textHomeStatus.text = "Status: ${it.status}"
                binding.textHomeStatus.setTextColor(if (it.status == ProviderStatus.ACTIVE) Color.GREEN else Color.RED)
                status = it.status
            }
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
                        vm.updateProviderLocation(vm.providerId, location.asGeoPoint().asLocation())
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

    private fun init() {
        binding.fabMarkCurrent.setOnClickListener {
            markCurrentLocation(true)
        }
        binding.fabHomeEdit.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEditFragment())
        }
        binding.cardToggle.setOnClickListener {
            status = status.flip()
            binding.textHomeStatus.text = if (status == ProviderStatus.ACTIVE) "Status: Active" else "Status: In Active"
            binding.textHomeStatus.setTextColor(if (status == ProviderStatus.ACTIVE) Color.GREEN else Color.RED)
            vm.updateProviderStatus(vm.providerId, status)
        }
    }

    private val polygonGeoPoints = arrayListOf<GeoPoint>()
    private lateinit var areaPolygon: Polygon

    private fun createCircleAroundPoint(point: GeoPoint, radius: Double) {
        Logger.d("Drawing circle for $point")
        polygonGeoPoints.clear()
        for (i in 0 until 360) {
            polygonGeoPoints.add(point.destinationPoint(radius * 1000, i.toDouble()))
        }
        handlePolygon()
    }

    private fun handlePolygon() {
        if (this::areaPolygon.isInitialized) {
            binding.map.overlayManager.remove(areaPolygon)
        }
        areaPolygon = Polygon().apply {
            title = "Sample Title"
            fillPaint.color = Color.BLUE
            fillPaint.alpha = 128
            outlinePaint.color = Color.BLACK
            isVisible = true
            points = polygonGeoPoints
        }
        binding.map.overlayManager.add(areaPolygon)
        binding.map.invalidate()
    }

    private fun addMarker(point: GeoPoint) {
        if (currentMarker != null) {
            binding.map.overlays.remove(currentMarker)
        }
        currentMarker = Marker(binding.map).apply {
            position = point
            icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(Color.RED) }
            setInfoWindow(null)
        }
        binding.map.overlays.add(currentMarker)
    }

}