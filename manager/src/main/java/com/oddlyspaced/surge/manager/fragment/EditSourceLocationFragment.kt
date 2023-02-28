package com.oddlyspaced.surge.manager.fragment

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.freelapp.libs.locationfetcher.LocationFetcher
import com.freelapp.libs.locationfetcher.locationFetcher
import com.google.android.gms.location.LocationRequest
import com.google.android.material.slider.Slider
import com.oddlyspaced.surge.app.common.AffinityConfiguration
import com.oddlyspaced.surge.app.common.Logger
import com.oddlyspaced.surge.app.common.asGeoPoint
import com.oddlyspaced.surge.app.common.asLocation
import com.oddlyspaced.surge.app.common.modal.Address
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.manager.BuildConfig
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.databinding.FragmentEditSourceLocationBinding
import com.oddlyspaced.surge.manager.viewmodel.ManagerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EditSourceLocationFragment: Fragment(R.layout.fragment_edit_source_location) {

    private lateinit var binding: FragmentEditSourceLocationBinding
    private lateinit var userLocationMarker: Marker
    private var currentMarker: Marker? = null
    private var currentAddress: Address? = null

    private val vm: ManagerViewModel by activityViewModels()

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

    /**
     * called when view is rendered for display
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentEditSourceLocationBinding.bind(view)
        initOSMDroid()
        CoroutineScope(Dispatchers.IO).launch {
            markCurrentLocation()
        }
        init()
        setupTouchTargetOverlay()
    }

    private fun init() {
        binding.selectEditLocation.txSelectLocation.text = "Pick center of serving area"
        binding.selectEditLocation.imgLocation.setColorFilter(Color.BLACK)

        binding.sliderEditDistance.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.textSearchDistance.text = "${value.toInt()} km"
            }
        }
        binding.sliderEditDistance.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                if (currentAddress == null) {
                    Toast.makeText(requireContext(), "Please select a source point by tapping on map first!", Toast.LENGTH_SHORT).show()
                }
                else {
                    createCircleAroundPoint(currentAddress!!.location.asGeoPoint(), slider.value.toDouble())
                }
            }
        })
        binding.cardEditSave.setOnClickListener {
            vm.sourcePointAddress = currentAddress
            vm.sourcePointWorkingRadius = binding.sliderEditDistance.value.toDouble()
            // todo: post this to server
//            homeViewModel.updateProviderSourceArea(1, homeViewModel.sourcePointAddress!!.location,
//                homeViewModel.sourcePointWorkingRadius
//            )
            findNavController().popBackStack()
        }

    }

    /**
     * handles runtime map configuration
     */
    private fun initOSMDroid() {
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

    // handle touches on map
    private fun setupTouchTargetOverlay() {
        val overlay = object : Overlay() {
            var items: ItemizedIconOverlay<OverlayItem>? = null

            override fun draw(pCanvas: Canvas?, pMapView: MapView?, pShadow: Boolean) {
            }

            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = mapView.projection
                val location = projection.fromPixels(e.x.toInt(), e.y.toInt())
                val lon = location.longitude
                val lat = location.latitude
                setAddressForLocation(Location(lat, lon))

                val markers = arrayListOf<OverlayItem>()
                val item = OverlayItem("", "", GeoPoint(lat, lon))
                item.setMarker(ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location))
                markers.add(item)

                if (items == null) {
                    items = ItemizedIconOverlay(requireActivity(), markers, null)
                    binding.map.overlays.add(items)
                    binding.map.invalidate()
                }
                else {
                    binding.map.overlays.remove(items)
                    binding.map.invalidate()
                    items = ItemizedIconOverlay(requireActivity(), markers, null)
                    binding.map.overlays.add(items)
                }
                createCircleAroundPoint(GeoPoint(lat, lon), binding.sliderEditDistance.value.toDouble())
                return true
            }
        }
        binding.map.overlays.add(overlay)
    }

    //// utils

    /**
     * location fetcher lib implementation to fetch current location and then mark it on the map as marker
     */
    private suspend fun markCurrentLocation() {
        locationFetcher.location.collectLatest {
            it.fold({ error ->
                Toast.makeText(requireContext(), "Error occurred while fetching location.", Toast.LENGTH_SHORT).show()
                Logger.d("ERROR: $error")
            }, { location ->
                val userPoint = location.asGeoPoint()
                if (this@EditSourceLocationFragment::userLocationMarker.isInitialized) {
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
        createCircleAroundPoint(point, binding.sliderEditDistance.value.toDouble())
    }

    private fun setAddressForLocation(location: Location) {
        binding.selectEditLocation.txSelectLocation.text = "Loading..."
        vm.addressFromLocation(location, binding.map.zoomLevelDouble.toInt()).observe(requireActivity()) { result ->
            binding.selectEditLocation.txSelectLocation.text = result.displayName + "\n" + "lat: ${location.lat}, lon: ${location.lon}"
            currentAddress = Address(location, result.displayName ?: "${location.lat}, ${location.lon}")
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

}