package com.oddlyspaced.surge.app.provider.fragment

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
import com.oddlyspaced.surge.app.common.AffinityConfiguration
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.asGeoPoint
import com.oddlyspaced.surge.app.common.modal.ProviderStatus
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.app.common.modal.flip
import com.oddlyspaced.surge.app.provider.BuildConfig
import com.oddlyspaced.surge.app.provider.R
import com.oddlyspaced.surge.app.provider.databinding.FragmentHomeBinding
import com.oddlyspaced.surge.app.provider.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var userLocationMarker: Marker
    private var currentMarker: Marker? = null

    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel: HomeViewModel by activityViewModels()
    private var status = ProviderStatus.UNDEFINED

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

    override fun onResume() {
        super.onResume()
        homeViewModel.sourcePointAddress?.let { sourceAddress ->
            addMarker(sourceAddress.location.asGeoPoint())
            createCircleAroundPoint(sourceAddress.location.asGeoPoint(), homeViewModel.sourcePointWorkingRadius.toDouble())
            binding.textHomeStatus.text = if (status == ProviderStatus.ACTIVE) "Status: Active" else "Status: In Active"
            binding.textHomeStatus.setTextColor(if (status == ProviderStatus.ACTIVE) Color.GREEN else Color.RED)
            // todo: fix update provider status
//            homeViewModel.updateProviderStatus(0, isActive)
        } ?: run {
            homeViewModel.getProviderInfo(1).observe(requireActivity()) {
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
                    icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)?.apply { setTint(Color.BLUE) }
                    setInfoWindow(null)
                }
                requireActivity().runOnUiThread {
                    binding.map.controller.setCenter(userPoint)
                    binding.map.overlays.add(userLocationMarker)
                }
            })
        }

    }

    private fun init() {
        binding.fabMarkCurrent.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                markCurrentLocation()
            }
        }
        binding.fabHomeEdit.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEditFragment())
        }
        binding.cardToggle.setOnClickListener {
            status = status.flip()
            binding.textHomeStatus.text = if (status == ProviderStatus.ACTIVE) "Status: Active" else "Status: In Active"
            binding.textHomeStatus.setTextColor(if (status == ProviderStatus.ACTIVE) Color.GREEN else Color.RED)
            // todo: handle update status
//            homeViewModel.updateProviderStatus(1, isActive)
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