package com.oddlyspaced.surge.manager.fragment

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.freelapp.libs.locationfetcher.locationFetcher
import com.oddlyspaced.surge.app.common.*
import com.oddlyspaced.surge.app.common.modal.AreaServed
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.PhoneNumber
import com.oddlyspaced.surge.app.common.modal.asGeoPoint
import com.oddlyspaced.surge.manager.BuildConfig
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.databinding.FragmentAddEditBinding
import com.oddlyspaced.surge.manager.viewmodel.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon

@AndroidEntryPoint
class AddEditFragment: Fragment(R.layout.fragment_add_edit) {

    private lateinit var userLocationMarker: Marker
    private lateinit var currentMarker: Marker

    private val polygonGeoPoints = arrayListOf<GeoPoint>()
    private lateinit var areaPolygon: Polygon

    private lateinit var binding: FragmentAddEditBinding
    private val vm: ManagerViewModel by activityViewModels()

    private val locationFetcher = locationFetcher("We need your permission to use your location for showing nearby items") {
        this.applyFrom(AffinityConfiguration.locationFetcherGlobalConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAddEditBinding.bind(view)
        initOSMDroid()
        if (vm.sourcePointAddress == null) {
            CoroutineScope(Dispatchers.IO).launch {
                markCurrentLocation()
            }
        }
        else {
            markSelectedSourceLocation()
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
                if (this@AddEditFragment::userLocationMarker.isInitialized) {
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

    private fun init() {
        binding.viewTouchMap.setOnClickListener {
            findNavController().navigate(AddEditFragmentDirections.actionAddFragmentToEditFragment())
        }

        binding.btnAddEditSave.setOnClickListener {
            handleSave()
        }
    }

    private fun handleSave() {
        binding.etAddEditName.text.let { name ->
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
                return
            }
        }
        binding.etAddEditCode.text.let { code ->
            if (code.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a phone number code", Toast.LENGTH_SHORT).show()
                return
            }
        }
        binding.etAddEditPhone.text.let { phone ->
            if (phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show()
                return
            }
        }
        binding.etAddEditServices.text.let { services ->
            if (services.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter services list", Toast.LENGTH_SHORT).show()
                return
            }
//            if (services.split(",").toTypedArray())
        }
        vm.sourcePointAddress?.let {} ?: run {
            Toast.makeText(requireContext(), "Please select area to be served", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            locationFetcher.location.collectLatest {
                it.fold({ error ->
                    Toast.makeText(requireContext(), "Error occurred while fetching location.", Toast.LENGTH_SHORT).show()
                    Logger.d("ERROR: $error")
                }, { location ->
                    requireActivity().runOnUiThread {
                        binding.layoutAddSaving.isVisible = true
                        vm.addProvider(
                            name = binding.etAddEditName.text.toString(),
                            phone = PhoneNumber(
                                countryCode = binding.etAddEditCode.text.toString(),
                                phoneNumber = binding.etAddEditPhone.text.toString()
                            ),
                            location = location.asGeoPoint().asLocation(),
                            services = arrayListOf<String>().apply {
                                addAll(
                                    binding.etAddEditServices.text.toString().split(",")
                                )
                            },
                            areaServed = AreaServed(vm.sourcePointAddress!!.location, vm.sourcePointWorkingRadius)
                        ).observe(requireActivity()) { response ->
                            if (response.error) {
                                Toast.makeText(
                                    requireContext(),
                                    "An error occured while trying to save provider!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.layoutAddSaving.isVisible = false
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Provider saved successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().popBackStack()
                            }
                        }
                    }
                })
            }

        }
    }

    private fun markSelectedSourceLocation() {
        vm.sourcePointAddress?.location?.let { location ->
            var items: ItemizedIconOverlay<OverlayItem>? = null
            val markers = arrayListOf<OverlayItem>()
            val item = OverlayItem("", "", GeoPoint(location.lat, location.lon))
            item.setMarker(ContextCompat.getDrawable(requireContext(), com.oddlyspaced.surge.app.common.R.drawable.ic_location))
            markers.add(item)
            items = ItemizedIconOverlay(requireActivity(), markers, null)
            Toast.makeText(requireContext(), "Marking selected location", Toast.LENGTH_SHORT).show()
            requireActivity().runOnUiThread {
                binding.map.controller.setZoom(AffinityConfiguration.DEFAULT_MAP_ZOOM)
                binding.map.controller.setCenter(GeoPoint(location.lat, location.lon))
                binding.map.overlays.add(items)
                binding.map.invalidate()
                createCircleAroundPoint(GeoPoint(location.lat, location.lon), vm.sourcePointWorkingRadius)
            }
        }
    }

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