package com.oddlyspaced.surge.app.customer.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.freelapp.libs.locationfetcher.locationFetcher
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.oddlyspaced.surge.app.common.*
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.Providers
import com.oddlyspaced.surge.app.common.modal.SearchParameter
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.app.customer.R
import com.oddlyspaced.surge.app.customer.databinding.FragmentSearchBinding
import com.oddlyspaced.surge.app.customer.viewmodel.CustomerViewModel
import com.oddlyspaced.surge.app.customer.viewmodel.LocationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class SearchFragment: Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding
    private val vm: CustomerViewModel by activityViewModels()
    private val markers = hashMapOf<Int, Marker>()
    private val locationFetcher = locationFetcher("We need permission to fetch location") {
        this.applyFrom(AffinityConfiguration.locationFetcherGlobalConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSearchBinding.bind(view)

        init()
        initOSMDroid()
        CoroutineScope(Dispatchers.Default).launch {
            markUserLocation()
        }
    }

    private fun init() {
        binding.sliderSearchDistance.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.textSearchDistance.text = "${value.toInt()} km"
            }
        }
        binding.sliderSearchDistance.addOnSliderTouchListener(object : OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                createCircleAroundPoint(vm.selectedLocation[LocationType.PICKUP]!!.location.asGeoPoint(), slider.value.toDouble())
            }

        })

        vm.providers.observe(requireActivity()) { list ->
            list.forEach { provider ->
                markProvider(provider)
            }
        }

        binding.chipgroupSearchService.addView(Chip(requireContext()).apply {
            text = "Loading..."
        })

        vm.fetchServices().observe(requireActivity()) { services ->
            binding.chipgroupSearchService.removeAllViews()
            services.forEach { service ->
                binding.chipgroupSearchService.addView(Chip(requireContext()).apply {
                    text = service.tag
//                    isCheckable = true
                })
            }
        }

        vm.selectedLocation[LocationType.PICKUP]?.let { pickupAddress ->
            markPickupLocation(pickupAddress.location.asGeoPoint())
        }

        vm.selectedLocation[LocationType.DROP]?.let { dropAddress ->
            markDropLocation(dropAddress.location.asGeoPoint())
        }

        binding.cardHomeSearch.setOnClickListener {
            try {
                val pickup = vm.selectedLocation[LocationType.PICKUP]!!.location
                val drop = vm.selectedLocation[LocationType.DROP]!!.location
                vm.search(
                    SearchParameter(
                        10,
                        binding.sliderSearchDistance.value.toInt(),
                        pickup.lat,
                        pickup.lon,
                        drop.lat,
                        drop.lon,
                    )
                ).observe(requireActivity()) {
                    Logger.d("Search res: ${it.size} \n $it.toStr")
                    findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToProviderDetailsFragment(Providers(it), markers[PickLocationFragment.MARKER_ID_USER]!!.position!!.asLocation()))
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

    private fun addMarker(id: Int, point: GeoPoint, tint: Int, onMarkerClick: ((Marker, MapView) -> Boolean)) {
        if (!isAdded) {
            return
        }
        requireActivity().runOnUiThread {
            markers[id]?.let { marker ->
                binding.map.overlays.remove(marker)
            }
            markers[id] = Marker(binding.map).apply {
                position = point
                setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location)
                    ?.apply { setTint(tint) }
                setOnMarkerClickListener(onMarkerClick)
                setInfoWindow(null)
            }
            binding.map.overlays.add(markers[id])
        }
    }

    private fun markProvider(provider: Provider) {
        addMarker(provider.id, provider.location.asGeoPoint(), Color.BLACK, ) { _, _ -> false }
    }

    private suspend fun markUserLocation() {
        locationFetcher.location.collectLatest {
            it.fold({ error ->
                requireActivity().runOnUiThread {
                    toast("Error occurred while fetching location.")
                }
                Logger.d("ERROR: $error")
            }, { location ->
                val userPoint = location.asGeoPoint()
                addMarker(PickLocationFragment.MARKER_ID_USER, userPoint, Color.BLUE) { _, _ -> false}
                requireActivity().runOnUiThread {
                    binding.map.controller.setCenter(userPoint)
                }
            })
        }
    }

    private fun markPickupLocation(point: GeoPoint) {
        addMarker(PickLocationFragment.MARKER_ID_PICKUP, point, Color.RED) { _, _, -> false}
        requireActivity().runOnUiThread {
            binding.map.controller.setCenter(point)
        }
    }

    private fun markDropLocation(point: GeoPoint) {
        addMarker(PickLocationFragment.MARKER_ID_DROP, point, Color.GREEN) { _, _, -> false}
        requireActivity().runOnUiThread {
            binding.map.controller.setCenter(point)
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