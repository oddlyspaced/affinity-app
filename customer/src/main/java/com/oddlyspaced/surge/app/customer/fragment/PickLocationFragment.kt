package com.oddlyspaced.surge.app.customer.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.freelapp.libs.locationfetcher.locationFetcher
import com.oddlyspaced.surge.app.common.*
import com.oddlyspaced.surge.app.common.modal.Address
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.app.customer.R
import com.oddlyspaced.surge.app.customer.databinding.FragmentPickLocationBinding
import com.oddlyspaced.surge.app.customer.viewmodel.HomeViewModel
import com.oddlyspaced.surge.app.customer.viewmodel.LocationType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@AndroidEntryPoint
class PickLocationFragment : Fragment(R.layout.fragment_pick_location) {

    private lateinit var binding: FragmentPickLocationBinding

    private val vm: HomeViewModel by activityViewModels()
    private var currentAddress: Address? = null
    private val args: PickLocationFragmentArgs by navArgs()

    companion object {
        const val MARKER_ID_PICKUP = -1
        const val MARKER_ID_DROP = -2
        const val MARKER_ID_USER = 0
    }

    private val markers = hashMapOf<Int, Marker>()

    private val locationFetcher = locationFetcher("We need permission to fetch location") {
        this.applyFrom(AffinityConfiguration.locationFetcherGlobalConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPickLocationBinding.bind(view)

        initOSMDroid()
        CoroutineScope(Dispatchers.IO).launch {
            markCurrentLocation()
        }
        setupTouchTargetOverlay()
        init()
    }

    // handles runtime map configuration
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

    private suspend fun markCurrentLocation() {
        locationFetcher.location.collectLatest {
            it.fold({ error ->
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error occurred while fetching location.", Toast.LENGTH_SHORT).show()
                }
                Logger.d("ERROR: $error")
            }, { location ->
                val userPoint = location.asGeoPoint()
                markUserLocation(userPoint)
            })
        }
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

    private fun markProvider(id: Int, provider: Provider) {
        addMarker(id, provider.location.asGeoPoint(), Color.BLACK, ) { _, _ -> false }
    }

    private fun markUserLocation(point: GeoPoint) {
        addMarker(MARKER_ID_USER, point, Color.BLUE) {_, _ -> false}
        requireActivity().runOnUiThread {
            binding.map.controller.setCenter(point)
        }
    }

    private fun markPickupLocation(point: GeoPoint) {
        addMarker(MARKER_ID_PICKUP, point, Color.RED) {_, _, -> false}
        requireActivity().runOnUiThread {
            binding.map.controller.setCenter(point)
        }
    }

    private fun markDropLocation(point: GeoPoint) {
        addMarker(MARKER_ID_DROP, point, Color.GREEN) {_, _, -> false}
        requireActivity().runOnUiThread {
            binding.map.controller.setCenter(point)
        }
    }

    // handle touches on map
    private fun setupTouchTargetOverlay() {
        val overlay = object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = binding.map.projection
                val loc = projection.fromPixels(e.x.toInt(), e.y.toInt())
                setAddressForLocation(loc.asGeoPoint().asLocation())
                Logger.d("${loc.latitude} | ${loc.longitude}")
                binding.map.controller.setCenter(loc.asGeoPoint())
                when (args.pickupType) {
                    LocationType.PICKUP -> markPickupLocation(loc.asGeoPoint())
                    LocationType.DROP -> markDropLocation(loc.asGeoPoint())
                }
                return true
            }
        }
        binding.map.overlays.add(overlay)
    }

    private fun init() {
        binding.txPickerTitle.text = "Select ${args.pickupType.name.lowercase()} location"
        binding.cardButtonSaveLocation.setOnClickListener {
            currentAddress?.let { address ->
                vm.selectedLocation[args.pickupType] = address
            }
            findNavController().popBackStack()
        }

        vm.providers.observe(requireActivity()) { list ->
            list.forEach { provider ->
                markProvider(provider.id, provider)
            }
        }

        vm.selectedLocation[LocationType.PICKUP]?.let { pickupAddress ->
            markPickupLocation(pickupAddress.location.asGeoPoint())
        }

        vm.selectedLocation[LocationType.DROP]?.let { dropAddress ->
            markDropLocation(dropAddress.location.asGeoPoint())
        }
    }

    private fun setAddressForLocation(location: Location) {
        binding.txPickerAddress.text = "Loading..."
        vm.addressFromLocation(location, binding.map.zoomLevelDouble.toInt()).observe(requireActivity()) { result ->
            binding.txPickerAddress.text = result.displayName + "\n" + "lat: ${location.lat}, lon: ${location.lon}"
            currentAddress = Address(location, result.displayName ?: "${location.lat}, ${location.lon}")
        }
    }

}